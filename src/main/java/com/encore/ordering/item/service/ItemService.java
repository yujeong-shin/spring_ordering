package com.encore.ordering.item.service;

import com.encore.ordering.item.domain.Item;
import com.encore.ordering.item.dto.ItemReqDto;
import com.encore.ordering.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item create(ItemReqDto itemReqDto){
        MultipartFile multipartFile = itemReqDto.getItemImage();
        String fileName = multipartFile.getOriginalFilename();
        Item new_item = Item.builder()
                .name(itemReqDto.getName())
                .category(itemReqDto.getCategory())
                .price(itemReqDto.getPrice())
                .stockQuantity(itemReqDto.getStockQuantity())
                .build();
        Item item = itemRepository.save(new_item);
        Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", item.getId() +"_"+ fileName);
        item.setImagePath(path.toString());

        //파일에 들어갔는데 DB에는 안들어간 경우 방지 -> 예외처리!!!
        try {
            byte[] bytes = multipartFile.getBytes();
            // 파일에 같은 이름을 가진 사진이 있으면 덮어쓰기, 없으면 create
            // checked exception은 예외가 안터져서 DB에 들어가지 못해도 rollback이 안된다.
            // unchecked exception으로 바꿔줘서 꼭 예외를 터트려야 한다
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) { //파일처리는 런타임 에러를 무조건!! 발생시켜줘야 한다.
            throw new IllegalArgumentException("image is not available");
        }
        return item;
    }

    public Item delete(Long id){
        Item item = itemRepository.findById(id).orElseThrow(()->new EntityNotFoundException("not found item"));
        item.deleteItem();
        return item; //update하면 수정된 item이 return => transactional이 걸려 dirtychecking
    }
}
