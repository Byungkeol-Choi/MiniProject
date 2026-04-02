//package com.cafe.kiosk.controller;
//
//import com.cafe.kiosk.dto.MenuDto;
//import com.cafe.kiosk.service.MenuService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@Controller
//@RequiredArgsConstructor
//public class AdminMenuController {
//  private final MenuService menuService;
//
//  //메뉴 목록
//  @GetMapping("/admin/menus")
//  public String menus(Model model){
//    model.addAttribute("menus", menuService.findAll());
//    return "admin/menus";
//  }
//  // 메뉴등록
//  @PostMapping("/admin/menus")
//  @ResponseBody
//  public ResponseEntity<?> create(@RequestBody MenuDto dto){
//    menuService.saveFromDto(dto);
//    return ResponseEntity.ok(Map.of("success",true));
//  }
//  // 메뉴수정
//  @PutMapping("/admin/menus/{id}")
//  @ResponseBody
//  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MenuDto dto){
//    menuService.updateFromDto(id, dto);
//  }
//  // 메뉴삭제
//  @DeleteMapping("/admin/menus/{id}")
//  @ResponseBody
//  public ResponseEntity<?> delete(@PathVariable Long id){
//    menuService.delete(id);
//    return ResponseEntity.ok(Map.of("success",true));
//  }
//  // 판매 상태변경
//  @PatchMapping("/admin/menus/{id}/avilable")
//  @ResponseBody
//  public ResponseEntity<?> toggleAvailable(@PathVariable Long id, @RequestBody Map<String,Boolean> body){
//    menuService.setAvailable(id, body.get("available"));
//    return ResponseEntity.ok(Map.of("success",true));
//  }
//
//}
