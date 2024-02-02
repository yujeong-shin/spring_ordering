package com.encore.ordering.item.controller;

import com.encore.ordering.common.CommonResponse;
import com.encore.ordering.item.domain.Item;
import com.encore.ordering.item.dto.ItemReqDto;
import com.encore.ordering.item.dto.ItemResDto;
import com.encore.ordering.item.dto.ItemSearchDto;
import com.encore.ordering.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ItemController {
    private final ItemService itemService;
    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/item/create")
    public ResponseEntity<CommonResponse> itemCreate(ItemReqDto itemReqDto){
        Item item = itemService.create(itemReqDto);

        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "item is successfully created", item.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/items") //SecurityConfig 안에서 인증 불필요 정의
    public ResponseEntity<List<ItemResDto>> items(ItemSearchDto itemSearchDto, Pageable pageable){
        List<ItemResDto> itemResDtos = itemService.findAll(itemSearchDto, pageable);
        return new ResponseEntity<>(itemResDtos, HttpStatus.OK);
    }

    @GetMapping("/item/{id}/image") //SecurityConfig 안에서 인증 불필요 정의
    //이미지는 Resource로 리턴
    public ResponseEntity<Resource> getImage(@PathVariable Long id){
        Resource resource = itemService.getImage(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        // headers, status, body 순으로 오버라이딩 된 ResponseEntity 사용
        // 이전에는 계속 body, status 매개변수만 사용했음
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/item/{id}/update")
    public ResponseEntity<CommonResponse> itemUpdate(@PathVariable Long id, ItemReqDto itemReqDto){
        Item item = itemService.update(id, itemReqDto);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "item successfully updated", item.getId()), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/item/{id}/delete")
    public ResponseEntity<CommonResponse> itemDelete(@PathVariable Long id){
        Item item = itemService.delete(id);
        return new ResponseEntity<>(
                new CommonResponse(HttpStatus.OK,
                        "item is successfully deleted", item.getId()), HttpStatus.OK);
    }
}
