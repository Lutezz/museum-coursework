package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CourseworkTest {
    @Test void indexForDemoDataIs5275() { assertEquals(52.75, IndexCalculator.calculate(40, 55, 70)); }
    @Test void indexForMaxFactorsIs100() { assertEquals(100.0, IndexCalculator.calculate(100, 100, 100)); }
    @Test void indexRejectsFactorAbove100() { assertThrows(IllegalArgumentException.class, () -> IndexCalculator.calculate(101, 10, 10)); }
    @Test void indexRejectsNegativeFactor() { assertThrows(IllegalArgumentException.class, () -> IndexCalculator.calculate(10, -1, 10)); }
    @Test void newToAssessedAllowed() { assertTrue(ConservationRequestService.isAllowedTransition(RequestState.NEW, RequestState.ASSESSED)); }
    @Test void assessedToWorksAllowed() { assertTrue(ConservationRequestService.isAllowedTransition(RequestState.ASSESSED, RequestState.WORKS)); }
    @Test void worksToCompletedAllowed() { assertTrue(ConservationRequestService.isAllowedTransition(RequestState.WORKS, RequestState.COMPLETED)); }
    @Test void newToCompletedRejected() { assertFalse(ConservationRequestService.isAllowedTransition(RequestState.NEW, RequestState.COMPLETED)); }
    @Test void exhibitValidationWorks() { assertThrows(IllegalArgumentException.class, () -> ExhibitService.validate(new Exhibit(null, "", "Ваза", "кераміка", 40, 70))); }
    @Test void inspectionValidationWorks() { assertThrows(IllegalArgumentException.class, () -> InspectionService.validate(new Inspection(null, 1, "2026-10-05", 120))); }
}
