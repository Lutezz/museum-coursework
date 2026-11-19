# Інформаційна система контролю стану музейних експонатів

Курсова робота з дисципліни «Кросплатформене програмування».
Варіант №24. Виконав: Сластьонов Олександр Сергійович, група 202-ТН.
Національний університет «Полтавська політехніка імені Юрія Кондратюка», 2026–2027 н. р.

Клієнт–серверна система для обліку експонатів, їх оглядів, заявок на консервацію та фахівців.
Для кожної заявки сервер автоматично розраховує інтегральний показник пріоритету консервації
і контролює чотиристанний життєвий цикл заявки.

## Можливості

- CRUD для експонатів, оглядів, заявок на консервацію та фахівців.
- Інтегральний показник `I = 0,40·F1 + 0,35·F2 + 0,25·F3`, де F1 — чутливість матеріалу,
  F2 — тяжкість пошкодження, F3 — значущість експонування (усі фактори 0–100).
  F1 і F3 беруться з експоната, F2 — з огляду; I розраховує сервер.
- Життєвий цикл заявки: `NEW → ASSESSED → WORKS → COMPLETED`
  (НОВА → ОЦІНЕНО → РОБОТИ → ЗАВЕРШЕНО); інші переходи сервер відхиляє.
- Фоновий CSV-імпорт каталогу експонатів через JavaFX `Task` із `ProgressBar`.
- Валідація на клієнті й сервері, зрозумілі повідомлення про помилки, логування `java.util.logging`.
- 18 модульних тестів JUnit 5.

## Технології

| Компонент | Версія |
|---|---|
| Java | 21 |
| Maven | 3.x |
| JavaFX (javafx-controls) | 21.0.2 |
| Servlet API (`javax.servlet`) | 4.0.1 |
| Apache Tomcat | 9.x |
| MySQL / MySQL Connector/J | XAMPP / 9.7.0 |
| Jackson Databind | 2.18.2 |
| JUnit Jupiter | 5.11.4 |

Маршрут запиту: `JavaFX → HttpMuseumClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL`.

## Структура проєкту

```
museum_Slastonov/
├─ database.sql            створення БД museum_exhibits і демонстраційні дані
├─ README.md
├─ server/                 Maven-проєкт (packaging war)
│  ├─ pom.xml
│  └─ src/
│     ├─ main/java/ua/edu/nupp/kpp/server/
│     │     моделі, *DAO, *Service, IndexCalculator, *Servlet, JsonServlet, DBUtil
│     ├─ main/webapp/WEB-INF/web.xml
│     └─ test/java/ua/edu/nupp/kpp/server/CourseworkTest.java
└─ client/                 Maven-проєкт JavaFX
   ├─ pom.xml
   ├─ sample-data/         CSV-файли для імпорту
   └─ src/main/java/ua/edu/nupp/kpp/client/
         моделі, HttpMuseumClient, ApiException, ImportExhibitsTask, MainApp
```

## 1. Підготовка MySQL

1. У XAMPP Control Panel запустіть модулі **Apache** і **MySQL**.
2. Відкрийте http://localhost/phpmyadmin → вкладка **Імпорт** → виберіть `database.sql` → **Вперед**.
3. Буде створено базу `museum_exhibits` з таблицями `exhibit`, `inspection`, `specialist`,
   `conservation_request` і демонстраційними даними (2 експонати, 2 огляди, 2 фахівці, 2 заявки).
   Повторне виконання скрипту відновлює базу з нуля.

Параметри підключення сервер читає зі змінних середовища (у коді й репозиторії паролів немає):

| Змінна | За замовчуванням |
|---|---|
| `MUSEUM_DB_URL` | `jdbc:mysql://localhost:3306/museum_exhibits` |
| `MUSEUM_DB_USER` | `root` |
| `MUSEUM_DB_PASSWORD` | порожній |

Якщо для `root` задано пароль, виконайте один раз і перезапустіть IntelliJ IDEA та Tomcat:

```
setx MUSEUM_DB_PASSWORD "ваш_пароль"
```

## 2. Складання та запуск сервера

```
cd server
mvn clean package
```

Скопіюйте `server/target/museum-server.war` у каталог `webapps` Apache Tomcat 9.x і запустіть
`bin\startup.bat` (змінна `JAVA_HOME` має вказувати на JDK 21).

Перевірка: http://localhost:8080/museum-server/exhibits повинен повернути JSON-список експонатів.

| URL | GET | POST (`action`) |
|---|---|---|
| `/exhibits` | список експонатів | `add`, `update`, `delete` |
| `/inspections` | список оглядів | `add`, `update`, `delete` |
| `/requests` | список заявок | `add`, `update`, `delete`, `transition` |
| `/specialists` | список фахівців | `add`, `update`, `delete` |

Помилки даних повертаються як HTTP 400 з JSON `{"error": "..."}`, непередбачені — як HTTP 500.

## 3. Запуск JavaFX-клієнта

Запускайте з каталогу `client`, щоб діалог імпорту відкривався в `sample-data`:

```
cd client
mvn javafx:run
```

Або запустіть клас `ua.edu.nupp.kpp.client.MainApp` в IntelliJ IDEA з робочим каталогом `client`.
Порядок запуску: MySQL → Tomcat → JavaFX-клієнт.

## 4. Тести JUnit 5

```
cd server
mvn clean test
```

Очікуваний результат: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0` і `BUILD SUCCESS`.
Тести перевіряють інтегральний показник, переходи життєвого циклу та валідацію сутностей
і не потребують запущених MySQL, Tomcat чи JavaFX.

## 5. CSV-імпорт

Вкладка **Експонати** → кнопка **Імпортувати CSV** → виберіть файл.

Формат: UTF-8, роздільник `;`, перший рядок — заголовок:

```
inventory_number;name;material;material_sensitivity;exposure_significance
ЕК-003;Дерев'яна скринька;дерево;65;50
ЕК-004;Срібна таця;метал;30;80
```

| Стовпець | Тип | Обов'язковий |
|---|---|---|
| `inventory_number` | рядок, унікальний | так |
| `name` | рядок | так |
| `material` | рядок | так |
| `material_sensitivity` | число 0–100 (F1) | так |
| `exposure_significance` | число 0–100 (F3) | так |

Правила обробки:
- неправильний заголовок, відсутній файл або недоступний сервер — імпорт не виконується, показується причина;
- рядок з неправильною кількістю полів чи нечисловим значенням пропускається;
- рядок, який відхилив сервер (діапазон 0–100, наявний інвентарний номер), пропускається;
- причини пропусків записуються в журнал (WARNING з номером рядка).

Через унікальність інвентарного номера повторний імпорт того самого файлу не створює дублікатів.

Файли в `client/sample-data`:
- `exhibits.csv` — коректний каталог (2 експонати);
- `exhibits-errors.csv` — 1 коректний рядок і 4 рядки з помилками;
- `exhibits-wrong-header.csv` — переставлені стовпці заголовка.
