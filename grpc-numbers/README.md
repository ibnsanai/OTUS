### Шаг 1. Собрать проект

```cmd
mvn clean compile
```

### Шаг 2. Запустить в разных терминалах
```cmd
mvn exec:java -Pserver
```
```cmd
mvn exec:java -Pclient
```
