package com.sbt.task7mskclient.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientToClientFromDTO {
    private Long clientFromId;
    private Long clientToId;
}