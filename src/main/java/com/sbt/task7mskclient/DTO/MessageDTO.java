package com.sbt.task7mskclient.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    /**
     * ID диалога, в который сообщение отправляется
     */
    Long dialogSessionId;
    /**
     * Содержание сообщения
     */
    String text;
    /**
     * Никнейм отправителя сообщения
     */
    String nickname;

}