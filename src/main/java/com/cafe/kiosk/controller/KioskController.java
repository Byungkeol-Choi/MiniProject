package com.cafe.kiosk.controller;

import com.cafe.kiosk.domain.Menu;
import com.cafe.kiosk.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class KioskController {
  private final MenuService menuService;

  @GetMapping("/")
  public String index(Model model){
    model.addAttribute("foodMenus", menuService.findByCategory(Menu.Category.FOOD));
    model.addAttribute("drinkMenus", menuService.findByCategory(Menu.Category.DRINK));
    return "kiosk/index";
  }
}
