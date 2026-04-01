package com.cafe.kiosk.service;

import com.cafe.kiosk.domain.Menu;
import com.cafe.kiosk.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final MenuRepository menuRepository;
  // 메뉴 조회
  public List<Menu> findAll(){
    return menuRepository.findAll();
  }
  // 메뉴 등록
  public Menu save(Menu menu){
    return menuRepository.save(menu);
  }

//  // 메뉴 수정 만드는중...
//  @Transactional
//  public Menu update(Long id, Menu updateMenu){
//    Menu menu = menuRepository.findById(id)
//            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
//    menu.update(
//            updateMenu.getName(),
//            updateMenu.getPrice(),
//            updateMenu.getCategory(),
//            updateMenu.getDescription(),
//            updateMenu.getImageUrl()
//    );
//    return menu;
//  }

  // 메뉴 삭제
  public void delete(Long id){
    menuRepository.deleteById(id);
  }

  public List<Menu> findByCategory(Menu.Category category) {
    return menuRepository.findByCategory(category);
  }

}
