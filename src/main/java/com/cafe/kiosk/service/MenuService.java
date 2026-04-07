package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Menu;
import com.cafe.kiosk.dto.MenuDto;
import com.cafe.kiosk.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final MenuRepository menuRepository;
  // 메뉴 조회
  public List<Menu> findAll() {
    return menuRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
  }

  // 메뉴 삭제
  public void delete(Long id){
    menuRepository.deleteById(id);
  }

  public List<Menu> findByCategory(Menu.Category category) {
    return menuRepository.findByCategory(category);
  }

  //메뉴 등록 dto
  @Transactional
  public Menu saveFromDto(MenuDto dto){
    Menu menu = new Menu(
            dto.getName(),
            dto.getPrice(),
            dto.getCategory(),
            dto.getDescription(),
            dto.getImageUrl(),
            dto.isAvailable(),
            LocalDateTime.now()
    );
    return menuRepository.save(menu);
  }

  // 메뉴 수정 dto
  @Transactional
  public Menu updateFromDto(Long id, MenuDto dto){
    Menu menu = menuRepository.findById(id)
            .orElseThrow(()-> new IllegalArgumentException("not found: " + id));
    menu.update(dto.getName(), dto.getPrice(), dto.getCategory(),dto.getDescription(), dto.getImageUrl());
    return menu;
  }

  // 판매상태 변경 /품절확인
  @Transactional
  public void setAvailable(Long id, boolean availble){
    Menu menu = menuRepository.findById(id)
            .orElseThrow(()-> new IllegalArgumentException("not found: " + id));
    menu.setAvailable(availble);
  }

  public Menu findById(Long id) {
    return menuRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
  }


}
