### Spring Boot. Backend. Клиника для Хомяков

Схема базы данных:
![database](screenshots/db.png)

Добавлена связь между Записями к врачу и хомяками на случай если у хомяка сменится владелец.

#### Запуск
1. После клонировния репозитория переименуйте файл `.env.example` в `.env`. При желании можете поменять пароль к БД в поле `POSTGRES_PASSWORD`
2. Поднимите БД PostgreSql через команду (выполнять в корне проекта):
```bash
docker compose up
```
3. Запустите программу с помощью:
```bash
./mvnw spring-boot:run
```

#### Примеры REST запросов:
```bash
# Создать хозяина
curl -X POST http://localhost:8080/api/owners \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Кузнецова Анна","phone":"+7-901-100-10-10","email":"anna@example.com"}'
```

```bash
# Список
curl http://localhost:8080/api/owners
```

```bash
# Создать врача
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Иванов Иван","specialty":"Терапевт","email":"ivanov@vet.example"}'
```

```bash
# Создать хомяка, принадлежащего хозяину с id = 1
curl -X POST http://localhost:8080/api/hamsters \
  -H "Content-Type: application/json" \
  -d '{"name":"Бублик","ownerId":1}'
```

```bash
# Запись на приём
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"doctorId":1,"ownerId":1,"hamsterId":1,"appointmentDate":"2025-06-01T10:30:00"}'
```