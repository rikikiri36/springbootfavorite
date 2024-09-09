package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping("/houses")
public class HouseController {
    private final HouseRepository houseRepository;        
    private final FavoriteRepository favoriteRepository;      
    private final FavoriteService favoriteService;      
    
    
    public HouseController(HouseRepository houseRepository, FavoriteRepository favoriteRepository, FavoriteService favoriteService) {
        this.houseRepository = houseRepository;
        this.favoriteRepository = favoriteRepository;
        this.favoriteService = favoriteService;
    }     
  
    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "area", required = false) String area,
                        @RequestParam(name = "price", required = false) Integer price,  
                        @RequestParam(name = "order", required = false) String order,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) 
    {
        Page<House> housePage;
                
        if (keyword != null && !keyword.isEmpty()) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%", "%" + keyword + "%", pageable);
            } else {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%", "%" + keyword + "%", pageable);
            }            
        } else if (area != null && !area.isEmpty()) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
            } else {
                housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
            }            
        } else if (price != null) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
            } else {
                housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
            }            
        } else {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
            } else {
                housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);   
            }            
        }                
        
        model.addAttribute("housePage", housePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("area", area);
        model.addAttribute("price", price);
        model.addAttribute("order", order);
        
        return "houses/index";
    }

    @GetMapping("/{id}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PathVariable(name = "id") Integer id, Model model) {
    	User user = userDetailsImpl.getUser();
    	House house = houseRepository.getReferenceById(id);
    	Optional<Favorite> favorite = favoriteRepository.findFirstByUserAndHouseId(user, id);
//    	List<Favorite> favorite = favoriteRepository.findByUserAndHouseId(user, id);
        
        model.addAttribute("house", house);
        model.addAttribute("reservationInputForm", new ReservationInputForm());

        model.addAttribute("favorite", favorite);
        
        return "houses/show";
    }

//    お気に入り登録・解除		⭐︎favoritecontrollarを作るべき？
    @GetMapping("/editfavorite/{id}")
    public String toggleFavorite(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
System.out.println(id);
System.out.println(id.getClass().getSimpleName());
    	User user = userDetailsImpl.getUser();
 
    	// お気に入りの追加または削除処理
        try {
    	favoriteService.favoriteEdit(user, id);
        }
        catch(Exception e) {
        	System.out.println("エラー");
        }
        // 処理後にリダイレクト
        return "redirect:/houses/{id}";
    }
}
