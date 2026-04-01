package com.cafe.kiosk.repository;

import com.cafe.kiosk.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

  List<Menu> findByCategory(Menu.Category category);

  List<Menu> findByAvailableTrue();
}
