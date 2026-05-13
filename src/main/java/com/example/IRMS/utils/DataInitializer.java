package com.example.IRMS.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.IRMS.modules.admin_tools.enums.RoleType;
import com.example.IRMS.modules.admin_tools.models.UserEntity;
import com.example.IRMS.modules.admin_tools.repositories.UserRepository;
import com.example.IRMS.modules.digital_ordering.enums.DishCategory;
import com.example.IRMS.modules.digital_ordering.enums.StationType;
import com.example.IRMS.modules.digital_ordering.models.MenuItemEntity;
import com.example.IRMS.modules.digital_ordering.repositories.MenuRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Component 
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final MenuRepository menuRepository;
  
  @Override
  public void run(String... args) throws Exception {
    createUsersIfNotExists();
    createMenuItemsIfNotExists();
  }

  private void createMenuItemsIfNotExists() {
    if (menuRepository.count() > 0) return;

    List<MenuItemEntity> menu = new ArrayList<>();

    // ==========================================
    // APPETIZERS (10 Items)
    // ==========================================
    menu.add(createDish("Crispy Fries", DishCategory.APPETIZER, 6, 35000.0, 
      "Golden crispy fries seasoned to perfection.", List.of(StationType.FRYER)));
    menu.add(createDish("Caesar Salad", DishCategory.APPETIZER, 5, 45000.0, 
      "Crisp romaine lettuce with Caesar dressing, croutons, and parmesan.", List.of(StationType.SALAD)));
    menu.add(createDish("Crispy Spring Rolls", DishCategory.APPETIZER, 8, 50000.0, 
      "Deep-fried vegetable spring rolls served with sweet chili sauce.", List.of(StationType.FRYER)));
    menu.add(createDish("Beer-Battered Onion Rings", DishCategory.APPETIZER, 7, 40000.0, 
      "Thick-cut onion rings fried in a light, crispy beer batter.", List.of(StationType.FRYER)));
    menu.add(createDish("Caprese Salad", DishCategory.APPETIZER, 4, 60000.0, 
      "Fresh tomatoes, mozzarella, and basil drizzled with balsamic glaze.", List.of(StationType.SALAD)));
    menu.add(createDish("Garlic Bread", DishCategory.APPETIZER, 5, 30000.0, 
      "Toasted baguette slices with garlic herb butter.", List.of(StationType.GENERAL)));
    
    // Multi-Station Appetizers
    menu.add(createDish("Spicy Buffalo Wings", DishCategory.APPETIZER, 12, 75000.0, 
      "Fried wings tossed in buffalo sauce, finished in the oven.", List.of(StationType.FRYER, StationType.GENERAL)));
    menu.add(createDish("Loaded Nachos", DishCategory.APPETIZER, 10, 85000.0, 
      "Tortilla chips topped with melted cheese, jalapeños, and grilled ground beef.", List.of(StationType.GENERAL, StationType.GRILL)));
    menu.add(createDish("Crispy Calamari", DishCategory.APPETIZER, 6, 65000.0, 
      "Lightly dusted and fried calamari rings with marinara dip.", List.of(StationType.FRYER)));
    menu.add(createDish("Tomato Bruschetta", DishCategory.APPETIZER, 6, 45000.0, 
      "Toasted bread topped with fresh tomato, basil, and garlic salad.", List.of(StationType.GENERAL, StationType.SALAD)));

    // ==========================================
    // MAIN COURSES (14 Items)
    // ==========================================
    menu.add(createDish("Crabstick Cocktail Pizza", DishCategory.MAIN_COURSE, 12, 179000.0, 
      "An innovative pizza topped with crabstick cocktail and mozzarella.", List.of(StationType.GENERAL)));
    menu.add(createDish("Pepperoni Pizza", DishCategory.MAIN_COURSE, 10, 179000.0, 
      "Classic pepperoni pizza with a crispy crust and melted mozzarella.", List.of(StationType.GENERAL)));
    menu.add(createDish("Margherita Pizza", DishCategory.MAIN_COURSE, 10, 150000.0, 
      "Simple and classic with fresh tomatoes, mozzarella, and basil.", List.of(StationType.GENERAL)));
    menu.add(createDish("Bacon Cheese Burger", DishCategory.MAIN_COURSE, 8, 120000.0, 
      "Juicy beef patty topped with bacon, cheddar, lettuce and tomato.", List.of(StationType.GRILL)));
    menu.add(createDish("Grilled Salmon", DishCategory.MAIN_COURSE, 15, 250000.0, 
      "Fresh Atlantic salmon fillet grilled to perfection.", List.of(StationType.GRILL)));
    menu.add(createDish("Spaghetti Carbonara", DishCategory.MAIN_COURSE, 12, 150000.0, 
      "Classic Italian pasta with creamy egg sauce and pancetta.", List.of(StationType.GENERAL)));
    menu.add(createDish("Wild Mushroom Risotto", DishCategory.MAIN_COURSE, 20, 185000.0, 
      "Creamy Arborio rice slow-cooked with wild mushrooms.", List.of(StationType.GENERAL)));
    menu.add(createDish("Classic Lasagna", DishCategory.MAIN_COURSE, 18, 160000.0, 
      "Layers of pasta, meat sauce, ricotta, and mozzarella cheese.", List.of(StationType.GENERAL)));
      menu.add(createDish("Chicken Fajitas", DishCategory.MAIN_COURSE, 15, 175000.0, 
      "Sizzling grilled chicken breast strips with bell peppers and onions.", List.of(StationType.GRILL)));
      
      // Multi-Station Mains
      menu.add(createDish("Burger & Fries Combo", DishCategory.MAIN_COURSE, 10, 145000.0, 
      "Our classic Bacon Cheese Burger served with crispy fries.", List.of(StationType.GRILL, StationType.FRYER)));
      menu.add(createDish("BBQ Pork Ribs with Onion Rings", DishCategory.MAIN_COURSE, 25, 280000.0, 
      "Slow-cooked ribs with beer-battered onion rings.", List.of(StationType.GRILL, StationType.FRYER)));
      menu.add(createDish("Grilled Chicken Caesar", DishCategory.MAIN_COURSE, 12, 95000.0, 
      "Our classic Caesar salad topped with grilled chicken.", List.of(StationType.SALAD, StationType.GRILL)));
      menu.add(createDish("Steak & Frites", DishCategory.MAIN_COURSE, 15, 320000.0, 
      "Grilled Ribeye steak served with a heap of crispy golden fries.", List.of(StationType.GRILL, StationType.FRYER)));
      menu.add(createDish("Fish and Chips", DishCategory.MAIN_COURSE, 14, 165000.0, 
      "Beer-battered cod fillet fried crispy, served with fries and tartar sauce.", List.of(StationType.FRYER))); // Note: Single station, but complex prep
      menu.add(createDish("Makima", DishCategory.MAIN_COURSE, 15, 1000000.0, 
        "Chainsaw's special.", List.of(StationType.GRILL, StationType.FRYER, StationType.SALAD, StationType.DESSERT, StationType.BEVERAGE))); // Joke item to test multi-station handling

    // ==========================================
    // DESSERTS (5 Items)
    // ==========================================
    menu.add(createDish("Chocolate Lava Cake", DishCategory.DESSERT, 7, 55000.0, 
      "Warm chocolate cake with a gooey molten center.", List.of(StationType.DESSERT)));
    menu.add(createDish("Classic Tiramisu", DishCategory.DESSERT, 5, 65000.0, 
      "Coffee-flavored Italian dessert with mascarpone cheese.", List.of(StationType.DESSERT)));
    menu.add(createDish("Matcha Green Tea Ice Cream", DishCategory.DESSERT, 2, 30000.0, 
      "Two scoops of rich and earthy matcha green tea ice cream.", List.of(StationType.DESSERT)));
    menu.add(createDish("New York Cheesecake", DishCategory.DESSERT, 3, 60000.0, 
      "A thick, creamy slice of classic vanilla cheesecake with graham crust.", List.of(StationType.DESSERT)));
    menu.add(createDish("Warm Brownie Sundae", DishCategory.DESSERT, 5, 55000.0, 
      "Fudge brownie topped with vanilla ice cream and chocolate syrup.", List.of(StationType.DESSERT)));

    // ==========================================
    // BEVERAGES (6 Items)
    // ==========================================
    menu.add(createDish("Fresh Lemonade", DishCategory.BEVERAGE, 2, 20000.0, 
      "Refreshing handmade lemonade with a hint of mint.", List.of(StationType.BEVERAGE)));
    menu.add(createDish("Vietnamese Iced Coffee", DishCategory.BEVERAGE, 3, 35000.0, 
      "Strong dark roast coffee with sweetened condensed milk over ice.", List.of(StationType.BEVERAGE)));
    menu.add(createDish("Tropical Mango Smoothie", DishCategory.BEVERAGE, 4, 45000.0, 
      "Blended fresh mango, yogurt, and a touch of honey.", List.of(StationType.BEVERAGE)));
    menu.add(createDish("Classic Cola", DishCategory.BEVERAGE, 1, 15000.0, 
      "Chilled classic cola served with ice and a slice of lemon.", List.of(StationType.BEVERAGE)));
    menu.add(createDish("Craft Draft Beer", DishCategory.BEVERAGE, 2, 60000.0, 
      "A pint of locally brewed IPA.", List.of(StationType.BEVERAGE)));
    menu.add(createDish("Mint Mojito", DishCategory.BEVERAGE, 3, 75000.0, 
      "Classic mocktail with muddled mint, lime, sugar, and club soda.", List.of(StationType.BEVERAGE)));

    menuRepository.saveAll(menu);
  }

  // Helper method to keep menu initialization clean and readable
  private MenuItemEntity createDish(String name, DishCategory category, Integer prepMinutes, 
                    Double price, String description, List<StationType> stations) {
    MenuItemEntity item = new MenuItemEntity();
    item.setName(name);
    item.setDishCategory(category);
    item.setEstimatedPrepMinutes(prepMinutes);
    item.setPrice(price);
    item.setAvailable(true);
    item.setDescription(description);
    item.setStations(stations);
    return item;
  }

  private void createUsersIfNotExists() {
    createUser("manager@irms.com", "System Manager", "manager123", RoleType.MANAGER);
    createUser("chef@irms.com", "Head Chef", "chef123", RoleType.CHEF);
    createUser("chef2@irms.com", "Sous Chef", "chef123", RoleType.CHEF);
    createUser("server@irms.com", "Server One", "server123", RoleType.SERVER);
    createUser("server2@irms.com", "Server Two", "server123", RoleType.SERVER);
    createUser("cashier@irms.com", "Front Cashier", "cashier123", RoleType.CASHIER);
  }

  private void createUser(String email, String name, String password, RoleType role) {
    if (userRepository.existsByEmail(email)) return;
  
    UserEntity user = new UserEntity();
    user.setName(name);
    user.setEmail(email);
    user.setRole(role);
    user.setHashedPassword(passwordEncoder.encode(password));
    userRepository.save(user);
  }
}