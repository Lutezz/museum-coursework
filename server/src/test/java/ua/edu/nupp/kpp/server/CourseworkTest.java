package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульні тести предметної логіки варіанта №24.
 * Перевіряються реальні методи IndexCalculator, ConservationRequestService та validate(...) сервісів;
 * MySQL, Tomcat і JavaFX для запуску не потрібні.
 */
public class CourseworkTest {
    private static final double EPS = 1e-9;

    // ---------- Інтегральний показник I = 0,40·F1 + 0,35·F2 + 0,25·F3 ----------

    @Test
    @DisplayName("I для демо-заявки №1 (кераміка): 0,40·40 + 0,35·55 + 0,25·70 = 52,75")
    void indexForDemoRequest1() {
        double index = IndexCalculator.calculate(40, 55, 70);
        assertEquals(52.75, index, EPS);
    }

    @Test
    @DisplayName("I для демо-заявки №2 (льон): 0,40·85 + 0,35·30 + 0,25·60 = 59,50")
    void indexForDemoRequest2() {
        double index = IndexCalculator.calculate(85, 30, 60);
        assertEquals(59.50, index, EPS);
    }

    @Test
    @DisplayName("Вагові коефіцієнти F1, F2, F3 дорівнюють 0,40, 0,35, 0,25")
    void indexWeights() {
        assertAll(
            () -> assertEquals(40.0, IndexCalculator.calculate(100, 0, 0), EPS),
            () -> assertEquals(35.0, IndexCalculator.calculate(0, 100, 0), EPS),
            () -> assertEquals(25.0, IndexCalculator.calculate(0, 0, 100), EPS)
        );
    }

    @Test
    @DisplayName("Межі шкали: усі фактори 0 → I = 0, усі фактори 100 → I = 100")
    void indexBounds() {
        assertEquals(0.0, IndexCalculator.calculate(0, 0, 0), EPS);
        assertEquals(100.0, IndexCalculator.calculate(100, 100, 100), EPS);
    }

    @Test
    @DisplayName("Фактор понад 100 відхиляється")
    void indexRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> IndexCalculator.calculate(101, 10, 10));
    }

    @Test
    @DisplayName("Від'ємний фактор відхиляється")
    void indexRejectsNegativeFactor() {
        assertThrows(IllegalArgumentException.class, () -> IndexCalculator.calculate(10, -1, 10));
    }

    // ---------- Життєвий цикл: NEW → ASSESSED → WORKS → COMPLETED ----------

    @Test
    @DisplayName("Дозволено NEW → ASSESSED")
    void newToAssessedAllowed() {
        assertTrue(ConservationRequestService.isAllowedTransition(RequestState.NEW, RequestState.ASSESSED));
    }

    @Test
    @DisplayName("Дозволено ASSESSED → WORKS")
    void assessedToWorksAllowed() {
        assertTrue(ConservationRequestService.isAllowedTransition(RequestState.ASSESSED, RequestState.WORKS));
    }

    @Test
    @DisplayName("Дозволено WORKS → COMPLETED")
    void worksToCompletedAllowed() {
        assertTrue(ConservationRequestService.isAllowedTransition(RequestState.WORKS, RequestState.COMPLETED));
    }

    @Test
    @DisplayName("Заборонено пропускати стани: NEW → COMPLETED")
    void skippingStateRejected() {
        assertFalse(ConservationRequestService.isAllowedTransition(RequestState.NEW, RequestState.COMPLETED));
    }

    @Test
    @DisplayName("Заборонено зворотний перехід: WORKS → ASSESSED")
    void backwardTransitionRejected() {
        assertFalse(ConservationRequestService.isAllowedTransition(RequestState.WORKS, RequestState.ASSESSED));
    }

    @Test
    @DisplayName("Зі стану COMPLETED переходів немає")
    void completedIsFinal() {
        for (RequestState next : RequestState.values()) {
            assertFalse(ConservationRequestService.isAllowedTransition(RequestState.COMPLETED, next),
                    "COMPLETED → " + next + " має бути заборонено");
        }
    }

    // ---------- Валідація основних сутностей ----------

    @Test
    @DisplayName("Експонат без інвентарного номера відхиляється")
    void exhibitWithoutInventoryNumberRejected() {
        Exhibit exhibit = new Exhibit(null, "", "Ваза", "кераміка", 40, 70);
        assertThrows(IllegalArgumentException.class, () -> ExhibitService.validate(exhibit));
    }

    @Test
    @DisplayName("Експонат із чутливістю матеріалу 120 відхиляється")
    void exhibitSensitivityOutOfRangeRejected() {
        Exhibit exhibit = new Exhibit(null, "ЕК-100", "Ваза", "кераміка", 120, 70);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ExhibitService.validate(exhibit));
        assertTrue(e.getMessage().contains("0..100"));
    }

    @Test
    @DisplayName("Коректний експонат проходить валідацію")
    void validExhibitAccepted() {
        Exhibit exhibit = new Exhibit(null, "ЕК-100", "Ваза", "кераміка", 40, 70);
        assertDoesNotThrow(() -> ExhibitService.validate(exhibit));
    }

    @Test
    @DisplayName("Огляд із тяжкістю пошкодження 120 відхиляється")
    void inspectionSeverityOutOfRangeRejected() {
        Inspection inspection = new Inspection(null, 1, "2026-10-05", 120);
        assertThrows(IllegalArgumentException.class, () -> InspectionService.validate(inspection));
    }

    @Test
    @DisplayName("Огляд із неіснуючою датою 2026-13-40 відхиляється")
    void inspectionInvalidDateRejected() {
        Inspection inspection = new Inspection(null, 1, "2026-13-40", 30);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> InspectionService.validate(inspection));
        assertTrue(e.getMessage().contains("РРРР-ММ-ДД"));
    }

    @Test
    @DisplayName("Фахівець без ПІБ відхиляється")
    void specialistWithoutNameRejected() {
        Specialist specialist = new Specialist(null, "  ", "реставратор текстилю");
        assertThrows(IllegalArgumentException.class, () -> SpecialistService.validate(specialist));
    }
}
