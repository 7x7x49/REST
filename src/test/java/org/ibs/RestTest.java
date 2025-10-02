package org.ibs;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;

public class RestTest {

    private static String jsessionId;

    @BeforeAll
    static void openFood() {
        RestAssured.baseURI = "http://localhost:8080";
        try {
            given()
                    .when()
                    .get("/")
                    .then()
                    .statusCode(200);
            System.out.println("-Сервер доступен-");

            // JSESSIONID с /food (чтобы POST и GET работали в одной сессии)
            jsessionId = given()
                    .when()
                    .get("/food")
                    .then()
                    .statusCode(200)
                    .extract()
                    .cookie("JSESSIONID");

        } catch (Exception e) {
            System.out.println("Сервер недоступен! Запустите qualit-sandbox.jar");
        }
    }

    @Test
    void testFoodAPI() {
        // Получаем начальный список товаров
        printYellow("✭ Начальный список товаров:");
        getAllFood();

        printYellow("✭ Добавляем товары...");

        // Корректные значения
        testAddFood("Редиска", "VEGETABLE", false);
        testAddFood("Банан", "FRUIT", true);

        // Пустые значения
        testAddFood("", "VEGETABLE", false);
        testAddFood("Апельсин", "", true);
        testAddFood("", "", true);

        // Наименование латиницей
        testAddFood("Banana", "FRUIT", true);
        testAddFood("Cucumber", "VEGETABLE", false);
        testAddFood("Radish", "VEGETABLE", true);

        // Использование цифр, пробелов и символов в Наименовании
        testAddFood("77", "VEGETABLE", false);
        testAddFood("Банан29", "FRUIT", true);
        testAddFood("145Банан", "FRUIT", false);
        testAddFood("Пер сик29", "FRUIT", true);
        testAddFood("?? 3335Баnan", "FRUIT", false);
        testAddFood(" 12245Баnan??", "FRUIT", true);
        testAddFood("!?;?*;№", "FRUIT", true);

        // Тип кириллицей
        testAddFood("Банан", "Фрукт", true);
        testAddFood("Огурец", "овощ", false);

        // Получаем финальный список товаров
        printYellow("✭ Финальный список товаров:");
        getAllFood();
    }

    void testAddFood(String name, String type, boolean exotic) {
        // Добавляем товары
        String jsonBody = "{\"name\": \"" + name + "\", \"type\": \"" + type + "\", \"exotic\": " + exotic + "}";

        Response response = given()
                .baseUri("http://localhost:8080")
                .cookie("JSESSIONID", jsessionId)
                .contentType("application/json")
                .accept("application/json")
                .body(jsonBody)
                .when()
                .post("/api/food");

        int statusCode = response.getStatusCode();

        if (statusCode == 200) {
            printGreen("Успешно добавлены: " + name + " | " + type + " | " + exotic);
        } else {
            printRed("Не добавлены: " + name + " | " + type + " | " + exotic);
            printPurple("   Ответ сервера: " + response.getBody().asString());
            printBlue("   Статус код: " + statusCode);
        }
    }

    void getAllFood() {
        Response response = given()
                .baseUri("http://localhost:8080")
                .cookie("JSESSIONID", jsessionId)
                .accept("application/json")
                .when()
                .get("/api/food");

        if (response.getStatusCode() == 200) {
            String responseBody = response.getBody().asString();

            if (responseBody.contains("[]")) {
                printBlue("Список товаров пуст.");
            } else {
                String formattedBody = responseBody
                        .replace("},{", "},\n{")
                        .replace("[{", "[\n{")
                        .replace("}]", "}\n]");
                printCyan(formattedBody);
            }
        } else {
            printRed("Ошибка при получении списка товаров!");
            printRed("   Статус код: " + response.getStatusCode());
        }
    }

    void printGreen(String text) { System.out.println("\u001B[32m" + text + "\u001B[0m"); }
    void printRed(String text) { System.out.println("\u001B[31m" + text + "\u001B[0m"); }
    void printPurple(String text) { System.out.println("\u001B[35m" + text + "\u001B[0m"); }
    void printBlue(String text) { System.out.println("\u001B[36m" + text + "\u001B[0m"); }
    void printYellow(String text) { System.out.println("\u001B[33m" + text + "\u001B[0m"); }
    void printCyan(String text) { System.out.println("\u001B[96m" + text + "\u001B[0m"); }
}
