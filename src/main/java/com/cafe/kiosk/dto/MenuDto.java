package com.cafe.kiosk.dto;

import com.cafe.kiosk.domain.Menu;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuDto {
  private String name;
  private int price;
  private Menu.Category category;
  private String description;
  private String imageUrl;
  private boolean available;
}
