package com.encore.ordering.item.service;

import com.encore.ordering.item.domain.Item;
import com.encore.ordering.item.dto.ItemReqDto;
import com.encore.ordering.item.dto.ItemResDto;
import com.encore.ordering.item.dto.ItemSearchDto;
import com.encore.ordering.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public Resource getImage(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(()->new EntityNotFoundException("not found item"));
        Path path = Paths.get(item.getImagePath());
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            // RuntimeException -> IllegalArgumentException
            // unchecked exception으로 바꿔서 꼭 예외 터트려주기
            throw new IllegalArgumentException("url form is not valid");
        }
        return resource;
    }

    public Item update(Long id, ItemReqDto itemReqDto) {
        Item item = itemRepository.findById(id).orElseThrow(()->new EntityNotFoundException("not found item"));

        MultipartFile multipartFile = itemReqDto.getItemImage();
        String fileName = multipartFile.getOriginalFilename();
        Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", item.getId() +"_"+ fileName);
        // ItemReqDto의 다른 속성들 + "image의 새로운 주소" update.
        item.updateItem(itemReqDto.getName(), itemReqDto.getCategory(), itemReqDto.getPrice(), itemReqDto.getStockQuantity(), path.toString());

        try {
            byte[] bytes = multipartFile.getBytes();
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalArgumentException("image is not available");
        }
        return item;
    }

    public List<ItemResDto> findAll(ItemSearchDto itemSearchDto, Pageable pageable) {
        // 검색을 위해 Specification 객체 사용
        // Specification 객체는 복잡한 쿼리를 명세를 이용해 정의하여 쉽게 생성
        // 검색과 pagination 동시에 해줌
        Specification<Item> spec = new Specification<Item>() {
            // root : 엔티티의 속성을 접근하기 위한 객체
            // builder : 쿼리를 생성하기 위한 객체
            @Override
            public Predicate toPredicate(Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // 쿼리를 만들기 위한 Predicate List 생성
                // if문에서 조건으로 걸려 있는 컬럼에 대해 생성 가능한 모든 조합의 쿼리를 담음
                List<Predicate> predicates = new ArrayList<>();
                if(itemSearchDto.getName() != null){ // 검색 조건에 NAME이 들어오면
                    // NAME LIKE "%값%"
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + itemSearchDto.getName() + "%"));
                }
                if(itemSearchDto.getCategory() != null){ // 검색 조건에 CATEGORY
                    // NAME CATEGORY "%값%"
                    predicates.add(criteriaBuilder.like(root.get("category"), "%" + itemSearchDto.getCategory() + "%"));
                }
                // 삭제되지 않은 item만 조회
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));

                //배열로 변환
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i = 0; i < predicates.size(); i++){
                    predicateArr[i] = predicates.get(i);
                }

                //배열에 담긴 모든 조합의 쿼리가 &로 합쳐짐
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Item> items = itemRepository.findAll(spec, pageable);
        List<Item> itemList = items.getContent();

        List<ItemResDto> itemResDtos = itemList.stream()
                .map(i -> ItemResDto.builder()
                .id(i.getId())
                .name(i.getName())
                .category(i.getCategory())
                .price(i.getPrice())
                .stockQuantity(i.getStockQuantity())
                .imagePath(i.getImagePath())
                .build()).collect(Collectors.toList());
        return itemResDtos;
    }
}
