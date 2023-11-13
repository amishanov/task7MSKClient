package com.sbt.task7mskclient;

import com.sbt.task7mskclient.DTO.ClientToClientFromDTO;
import com.sbt.task7mskclient.DTO.IdDTO;
import com.sbt.task7mskclient.DTO.NickNameDTO;
import com.sbt.task7mskclient.DTO.ResponseMessageDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class Task7MskClientApplication {
    // TODO не забыть обработку ошибок
    // TODO вынести валидацию ввода id в отдельный метод
    private static String nickname;
    private static String baseURL = "http://localhost:8080";
    private static Long myId;
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
                myId = registerClient(nickname).getId();
                System.out.println("Ваш ID:" + myId);
                break;
            }

        }
        String choice;
        while (true) {
            System.out.printf("Здравствуйте, %s. Выберите пункт меню:", nickname);
            System.out.println("1. Получить список ID пользователей");
            System.out.println("2. Получить список ваших диалогов");
            System.out.println("3. Получить уведомления");
            System.out.println("4. Начать новый диалог");
            System.out.println("5. Открыть диалог");
            System.out.println("6. Отправить сообщение в диалог");
            System.out.println("7. Удалить диалог по ID");
            choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    List<IdDTO> clients = getAllClients();
                    System.out.println("Список пользователей приложения:");
                    clients.forEach(c -> System.out.println("id: " + c.getId()));
                    break;
                case "2":
                    List<IdDTO> dialogs = getAllMyDialogs();
                    System.out.println("Список id ваших диалогов:");
                    dialogs.forEach(d -> System.out.println("id: " + d.getId()));
                    break;
                case "3":
                    List<IdDTO> notificationDialogList = getMyUpdates();
                    System.out.println("Для вас есть новые сообщения в диалогах (id):");
                    notificationDialogList.forEach(n -> System.out.println("id диалога: " + n.getId()));
                    break;
                case "4":
                    IdDTO newDialogId = startDialog(scanner);
                    if (newDialogId != null)
                        System.out.println("Вы создали новый диалог с id: " + newDialogId.getId());
                    break;
                case "5":

                    break;
                case "6":
                    stopDialog(scanner);
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
                .bodyToMono(new ParameterizedTypeReference<List<IdDTO>>() {
                });
        return response.block();
    }

    public static IdDTO registerClient(String nickname) {
        NickNameDTO nickNameDTO = new NickNameDTO(nickname);
        Mono<IdDTO> response = webClient.post()
                .uri("/clients")
                .body(BodyInserters.fromValue(nickNameDTO))
                .retrieve()
                .bodyToMono(IdDTO.class);
        return response.block();
    }

    public static List<IdDTO> getAllMyDialogs() {
        Mono<List<IdDTO>> response = webClient.get()
                .uri("/clients/{id}/dialogs", myId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<IdDTO>>() {
                });
        return response.block();
    }

    public static List<IdDTO> getMyUpdates() {
        Mono<List<IdDTO>> response = webClient.get()
                .uri("/notifications/{id}", myId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<IdDTO>>() {
                });
        return response.block();
    }

    public static IdDTO startDialog(Scanner scanner) {
        Long clientToId;
        System.out.println("Введите id пользователя, с которым хотите начать диалог:");
        try {
            clientToId = scanner.nextLong();
            if (clientToId.equals(myId) || clientToId < 0)
                throw new InputMismatchException();
        } catch (InputMismatchException ex) {
            System.out.println("id должен быть целым числом положительным числом, отличным от вашего id");
            return null;
        }
        ClientToClientFromDTO clientToClientFromDTO = new ClientToClientFromDTO(myId, clientToId);
        Mono<IdDTO> response = webClient.post()
                .uri("/dialogs/")
                .body(BodyInserters.fromValue(clientToClientFromDTO))
                .retrieve()
                .bodyToMono(IdDTO.class);
        IdDTO newDialogId = response.block();
        if (newDialogId != null && newDialogId.getId() == -1L) {
            System.out.println("Пользователя с таким id не существует");
            return null;
        }
        return newDialogId;
    }

    public static void stopDialog(Scanner scanner) {
        Long dialogId;
        System.out.println("Введите id диалога, который хотите удалить:");
        try {
            dialogId = scanner.nextLong();
            if (dialogId < 0)
                throw new InputMismatchException();
        } catch (InputMismatchException ex) {
            System.out.println("id должен быть целым числом положительным числом");
            return;
        }
        webClient.delete()
                .uri("/dialogs/{id}", dialogId)
                .retrieve()
                .toBodilessEntity()
                .block();
        System.out.printf("Диалог с id: %d удалён", dialogId);
    }

    // TODO добавить обработку отсутствия диалога (когда добавишь в сервер). Желательно ещё отучись от возврата null
    public static List<ResponseMessageDTO> openDialog(Scanner scanner) {
        System.out.println("Введите id диалога, который хотите посмотреть");
        Long dialogId;
        try {
            dialogId = scanner.nextLong();
            if (dialogId < 0)
                throw new InputMismatchException();
        } catch (InputMismatchException ex) {
            System.out.println("id должен быть целым числом положительным числом");
            return null;
        }
        Mono<List<ResponseMessageDTO>> response = webClient.get()
                .uri("/messages/{dialogId}/{clientId}", dialogId, myId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ResponseMessageDTO>>() {
                });
        return response.block();
    }

    public static void sendMessage(Scanner scanner) {
        System.out.println("Введите id диалога, который хотите посмотреть");
        Long dialogId;
        try {
            dialogId = scanner.nextLong();
            if (dialogId < 0)
                throw new InputMismatchException();
        } catch (InputMismatchException ex) {
            System.out.println("id должен быть целым числом положительным числом");
            return;
        }
        //TODO
    }
}
