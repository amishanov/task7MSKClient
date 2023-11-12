package com.sbt.task7mskclient;

import com.sbt.task7mskclient.DTO.IdDTO;
import com.sbt.task7mskclient.DTO.NickNameDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class Task7MskClientApplication {
    // TODO не забыть обработку ошибок
    private static String nickname;
    private static String baseURL = "http://localhost:8080";
    private static WebClient webClient;
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Task7MskClientApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
        webClient = WebClient.builder().baseUrl(baseURL).build();

        System.out.println("Для регистрации введите псевдоним:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            nickname = scanner.nextLine();
            if (nickname.isEmpty())
                System.out.println("Псевдоним должен быть непустой строкой");
            else {
                System.out.println("Ваш ID:" + registerClient(nickname));
                break;
            }

        }
        String choice;
        while (true) {
            System.out.println(String.format("Здравствуйте, %s. Выберите пункт меню:", nickname));
            System.out.println("1. Получить список ID пользователей");
            System.out.println("2. Получить список ваших диалогов");
            System.out.println("3. Получить уведомления");
            System.out.println("4. Начать новый диалог");
            System.out.println("5. Открыть диалог (Используйте для отправки сообщений)");
            System.out.println("6. Удалить диалог по ID");
            choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    List<IdDTO> clients = getAllClients();
                    System.out.println("Список пользователей приложения:");
                    clients.stream().forEach(c -> System.out.println("id: " + c.getId()));
                    break;
                case "2":
                    break;
                case "3":
                    break;
                case "4":
                    break;
                case "5":
                    break;
                case "6":
                    break;
                default:
                    System.out.println("Выберите вариант из представленных");
            }
        }
    }

    public static List<IdDTO> getAllClients() {
        Mono<List<IdDTO>> response = webClient.get()
                .uri("/clients")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<IdDTO>>() {});
        List<IdDTO> clients = response.block();
        return clients;
    }

    public static IdDTO registerClient(String nickname) {
        NickNameDTO nickNameDTO = new NickNameDTO(nickname);
        Mono<IdDTO> response = webClient.post()
                .uri("/clients")
                .body(BodyInserters.fromValue(nickNameDTO))
                .retrieve()
                .bodyToMono(IdDTO.class);
        IdDTO idDTO = response.block();
        return idDTO;
    }

}
