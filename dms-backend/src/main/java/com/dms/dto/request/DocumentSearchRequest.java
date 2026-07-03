package com.dms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchRequest {

    /** General full-text search term; if set, overrides all other filters. */
    private String search;

    private String title;
    private String documentNumber;
    private Long categoryId;
    private Long departmentId;
    private String status;
    private Long ownerId;

    private LocalDate fromDate;
    private LocalDate toDate;

    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}