# HSE Antiplag

## Описание проекта

В рамках контрольной работы была реализована система для анализа текстовых файлов с использованием микросервисной архитектуры. Система предоставляет возможности загрузки файлов, их анализа с вычислением статистики и генерации облака слов.

## Архитектура системы

Система построена на основе микросервисной архитектуры и включает три основных сервиса:

### 1. API Gateway (порт 8080)

Единая точка входа для всех клиентских запросов

**Особенности реализации:**
- Использует Spring Cloud Gateway для маршрутизации запросов
- Реализует паттерн "Backend for Frontend" с адаптацией DTO
- Использует реактивное программирование на базе Spring WebFlux
- Применяет `WebClient` для неблокирующих HTTP-запросов к downstream сервисам

**Ключевые компоненты:**
- `GatewayController.java` - REST контроллер с реактивными методами (`Mono<>`)
  - `uploadFile()` - проксирование загрузки файлов в file-storage-service
  - `analyzeFile()` - проксирование запросов анализа в file-analysis-service  
  - `downloadFile()` - проксирование скачивания файлов
  - `getWordCloudImage()` - получение изображений облака слов
- `WebClientConfig.java` - конфигурация HTTP клиентов с базовыми URL и таймаутами
- `dto/` - пакет с DTO классами для адаптации между сервисами:
  - `FileUploadResponse.java` - ответ при загрузке файла
  - `GatewayAnalysisResult.java` - результат анализа текста
- `application.yml` - конфигурация маршрутов Spring Cloud Gateway

### 2. File Storage Service (порт 9001)
**Роль:** Управление хранением файлов и метаданных

**Особенности реализации:**
- Реализована дедупликация файлов через SHA-256 хеширование
- Проверка существования физических файлов при дедупликации
- **Хранение метаданных:** PostgreSQL с JPA/Hibernate
- **Файловая система:** Локальное хранение с уникальными именами файлов (UUID + расширение)

**Ключевые компоненты:**
- `controller/FileController.java` - REST API для управления файлами
  - `uploadFile()` - загрузка файлов с дедупликацией
  - `downloadFile()` - скачивание файлов по ID
  - `getFileMetadata()` - получение метаданных файла
- `service/FileStorageServiceImpl.java` - бизнес-логика хранения файлов
  - Логика дедупликации с проверкой физического существования файлов
  - Управление жизненным циклом файлов
- `domain/FileEntity.java` - JPA сущность для метаданных файлов
- `repository/FileRepository.java` - Spring Data JPA репозиторий
  - `findByHash()` - поиск файлов по SHA-256 хешу для дедупликации
- `utils/FileHashUtil.java` - утилита для вычисления SHA-256 хешей
- `config/SwaggerConfig.java` - конфигурация OpenAPI документации

**Модель данных:**
```java
@Entity
@Table(name = "files")
public class FileEntity {
    @Id private UUID id;
    @Column(nullable = false) private String fileName;
    @Column(nullable = false) private String contentType;
    @Column(nullable = false) private Long size;
    @Column(nullable = false) private LocalDateTime uploadTimestamp;
    @Column(nullable = false) private String filePath;
    @Column(nullable = false, unique = true) private String hash;
}
```

### 3. File Analysis Service (порт 9090)

Проводит анализ текстового содержимого и генерирует визуализацию

**Ключевые компоненты:**
- `controller/AnalysisController.java` - REST API для анализа файлов
  - `analyzeFile()` - анализ текста по ID файла
  - `getWordCloudImage()` - получение изображения облака слов
- `service/FileAnalysisServiceImpl.java` - основная бизнес-логика анализа
  - `analyzeFile()` - координация процесса анализа с кешированием
  - `downloadFileContent()` - получение содержимого файла из file-storage-service
  - `performTextAnalysis()` - анализ текстовых метрик
  - `generateWordCloud()` - интеграция с QuickChart.io API
- `service/TextAnalysisService.java` - сервис для анализа текста
  - `calculateWordCount()` - подсчет слов с обработкой пунктуации
  - `calculateParagraphCount()` - подсчет абзацев с обработкой различных форматов переносов
  - `calculateCharacterCount()` - подсчет символов
- `domain/AnalysisResult.java` - JPA сущность для кеширования результатов
- `repository/AnalysisResultRepository.java` - Spring Data JPA репозиторий
  - `findByFileId()` - поиск кешированных результатов
- `dto/` - пакет с DTO классами:
  - `TextStatistics.java` - статистика текста (слова, символы, абзацы)
  - `AnalysisResponse.java` - полный ответ анализа с путем к облаку слов
- `config/WebClientConfig.java` - конфигурация HTTP клиентов для внешних API

## Технические решения

### Алгоритмы анализа текста

**Подсчет слов (`TextAnalysisService.calculateWordCount()`):**
- Разделение по пробелам с обработкой множественных пробелов
- Удаление знаков пунктуации через regex `[^\\p{L}\\p{Nd}]`
- Фильтрация пустых строк

**Подсчет абзацев (`TextAnalysisService.calculateParagraphCount()`):**
- Обработка различных форматов переносов строк: `\n`, `\r\n`, `\r`
- Нормализация к единому формату через `text.replaceAll("\\r\\n|\\r", "\n")`
- Разделение по двойным переносам строк `\n\n+`
- Фильтрация пустых абзацев

**Интеграция с QuickChart.io API:**
- Формирование URL с параметрами облака слов
- Асинхронные HTTP запросы через `WebClient` с timeout настройками
- Кастомные заголовки `User-Agent` для корректной работы с внешним API

### Дедупликация файлов

С этим возникло много проблем и ушло много времени на дебаг

**Алгоритм в `FileStorageServiceImpl.storeFile()`:**
1. Вычисление SHA-256 хеша входящего файла через `FileHashUtil.calculateSHA256()`
2. Поиск существующих файлов по хешу в базе данных
3. Проверка физического существования файла через `Files.exists()` и `Files.isReadable()`
4. При отсутствии физического файла:
   - Удаление битой записи из базы: `fileRepository.delete(existingEntity)`
   - Принудительное применение изменений: `fileRepository.flush()`
   - Создание нового файла с тем же хешем
5. При наличии файла - возврат существующего ID без дублирования

### Кеширование результатов анализа

**Стратегия кеширования в `FileAnalysisServiceImpl`:**
- Кеш на уровне `fileId` в таблице `analysis_results`
- Проверка существования результата через `analysisResultRepository.findByFileId()`
- При cache hit - обновление `updatedAt` timestamp
- При cache miss - выполнение полного анализа и сохранение результата
- Хранение пути к изображению облака слов для переиспользования

### Реактивное программирование

При разработки системы старался использовать новые для меня парадигмы, например реактивное программирование

```java
// Пример из API Gateway
public Mono<GatewayAnalysisResult> analyzeFile(@PathVariable String fileId) {
    return fileAnalysisServiceWebClient.get()
        .uri("/api/v1/analysis/{fileId}", fileId)
        .retrieve()
        .bodyToMono(GatewayAnalysisResult.class)
        .doOnSuccess(result -> logger.info("Success: {}", result))
        .doOnError(error -> logger.error("Error: {}", error.getMessage()));
}
```

**Преимущества реактивного подхода:**
- Неблокирующие HTTP запросы между микросервисами
- Высокая пропускная способность при большом количестве одновременных запросов
- Эффективное использование системных ресурсов (меньше потоков)

### Обработка ошибок

- Graceful degradation: при сбое генерации облака слов возвращается анализ без изображения
- Централизованная обработка исключений
- Подробное логирование для диагностики

### Контейнеризация

- Все сервисы в изолированных контейнерах
- Общая Docker сеть для межсервисного взаимодействия
- PostgreSQL с persistent volume для сохранности данных
- Health checks для контроля состояния сервисов

## Тестирование

### Покрытие тестами

- API Gateway (6 тестов)

- File Storage Service (17 тестов)

- File Analysis Service (8 тестов):**


**Всего: 31 тест**

## Результаты

Система полностью функциональна и соответствует основным требованиям контрольной работы. Реализована устойчивая микросервисная архитектура с правильным разделением ответственности между сервисами. Код покрыт тестами и готов к production развертыванию.

### Конфигурация развертывания

**Docker Compose структура:**
- `docker-compose.yml` - оркестрация всех сервисов
- Общая сеть `antiplag-network` для межсервисного взаимодействия
- Persistent volumes:
  - `postgres_data` - сохранность данных PostgreSQL
  - `file_storage_data` - сохранность загруженных файлов
- Health checks для мониторинга состояния сервисов

**Порты и маршрутизация:**
- API Gateway (8080) - единая точка входа
- File Storage Service (9001) - внутренний доступ
- File Analysis Service (9090) - внутренний доступ  
- PostgreSQL (5432) - доступ к базе данных

**Доступ к системе:**
- **Swagger UI:** http://localhost:8080/swagger-ui.html - интерактивная документация API
- **API Gateway:** http://localhost:8080 - единая точка входа для всех запросов
- **File Storage Service:** http://localhost:9001 (внутренний доступ)
- **File Analysis Service:** http://localhost:9090 (внутренний доступ)
- **PostgreSQL:** localhost:5432

**Команды для запуска:**
```bash
# Сборка и запуск
mvn clean package spring-boot:repackage -DskipTests
docker-compose up --build -d
```
