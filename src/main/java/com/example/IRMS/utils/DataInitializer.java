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

@Component 
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    
    @Override
    public void run(String... args) throws Exception {
        createManagerIfNotExists();
        createChefIfNotExists();
        createServerIfNotExists();
        createCashierIfNotExists();
        createMenuItemsIfNotExists();
    }

    private void createMenuItemsIfNotExists() {
        if (menuRepository.count() > 0) return;

        // Example menu items covering categories used by frontend ordering + KDS
        MenuItemEntity pizza1 = new MenuItemEntity();
        pizza1.setName("Crabstick Cocktail Pizza");
        pizza1.setDishCategory(DishCategory.MAIN_COURSE);
        pizza1.setEstimatedPrepMinutes(12);
        pizza1.setPrice(179000);
        pizza1.setAvailable(true);
        pizza1.setDescription("An innovative pizza topped with crabstick cocktail, mozzarella, and a blend of herbs.");
        pizza1.setStations(java.util.List.of(StationType.GENERAL));

        MenuItemEntity pizza2 = new MenuItemEntity();
        pizza2.setName("Pepperoni Pizza");
        pizza2.setDishCategory(DishCategory.MAIN_COURSE);
        pizza2.setEstimatedPrepMinutes(10);
        pizza2.setPrice(179000);
        pizza2.setAvailable(true);
        pizza2.setDescription("Classic pepperoni pizza with a crispy crust and melted mozzarella.");
        pizza2.setStations(java.util.List.of(StationType.GENERAL));

        MenuItemEntity burger = new MenuItemEntity();
        burger.setName("Bacon Cheese Burger");
        burger.setDishCategory(DishCategory.MAIN_COURSE);
        burger.setEstimatedPrepMinutes(8);
        burger.setPrice(120000);
        burger.setAvailable(true);
        burger.setDescription("Juicy beef patty topped with bacon, cheddar, lettuce and tomato.");
        burger.setStations(java.util.List.of(StationType.GRILL));

        MenuItemEntity fries = new MenuItemEntity();
        fries.setName("Crispy Fries");
        fries.setDishCategory(DishCategory.APPETIZER);
        fries.setEstimatedPrepMinutes(6);
        fries.setPrice(35000);
        fries.setAvailable(true);
        fries.setDescription("Golden crispy fries seasoned to perfection.");
        fries.setStations(java.util.List.of(StationType.FRYER));

        MenuItemEntity salad = new MenuItemEntity();
        salad.setName("Caesar Salad");
        salad.setDishCategory(DishCategory.APPETIZER);
        salad.setEstimatedPrepMinutes(5);
        salad.setPrice(45000);
        salad.setAvailable(true);
        salad.setDescription("Crisp romaine lettuce with Caesar dressing and parmesan.");
        salad.setStations(java.util.List.of(StationType.SALAD));

        MenuItemEntity cake = new MenuItemEntity();
        cake.setName("Chocolate Lava Cake");
        cake.setDishCategory(DishCategory.DESSERT);
        cake.setEstimatedPrepMinutes(7);
        cake.setPrice(55000);
        cake.setAvailable(true);
        cake.setDescription("Warm chocolate cake with a gooey molten center.");
        cake.setStations(java.util.List.of(StationType.DESSERT));

        MenuItemEntity drink = new MenuItemEntity();
        drink.setName("Fresh Lemonade");
        drink.setDishCategory(DishCategory.BEVERAGE);
        drink.setEstimatedPrepMinutes(2);
        drink.setPrice(20000);
        drink.setAvailable(true);
        drink.setDescription("Refreshing handmade lemonade with a hint of mint.");
        drink.setStations(java.util.List.of(StationType.BEVERAGE));

        // ==========================================
        // NEW ITEMS (Ready for R2 Images)
        // ==========================================
        MenuItemEntity wings = new MenuItemEntity();
        wings.setName("Buffalo Hot Wings");
        wings.setDishCategory(DishCategory.APPETIZER);
        wings.setEstimatedPrepMinutes(10);
        wings.setPrice(85000);
        wings.setAvailable(true);
        wings.setDescription("Crispy fried chicken wings tossed in a spicy buffalo sauce.");
        wings.setStations(java.util.List.of(StationType.FRYER));
        wings.setImageUrl(null); // Explicitly null until R2 upload

        MenuItemEntity garlicBread = new MenuItemEntity();
        garlicBread.setName("Cheesy Garlic Bread");
        garlicBread.setDishCategory(DishCategory.APPETIZER);
        garlicBread.setEstimatedPrepMinutes(5);
        garlicBread.setPrice(45000);
        garlicBread.setAvailable(true);
        garlicBread.setDescription("Toasted baguette slices with garlic butter and melted mozzarella.");
        garlicBread.setStations(java.util.List.of(StationType.GENERAL));
        garlicBread.setImageUrl(null);

        MenuItemEntity steak = new MenuItemEntity();
        steak.setName("Grilled Ribeye Steak");
        steak.setDishCategory(DishCategory.MAIN_COURSE);
        steak.setEstimatedPrepMinutes(15);
        steak.setPrice(350000);
        steak.setAvailable(true);
        steak.setDescription("Premium 250g ribeye steak grilled to your liking, served with mashed potatoes.");
        steak.setStations(java.util.List.of(StationType.GRILL));

        MenuItemEntity pasta = new MenuItemEntity();
        pasta.setName("Seafood Linguine");
        pasta.setDishCategory(DishCategory.MAIN_COURSE);
        pasta.setEstimatedPrepMinutes(12);
        pasta.setPrice(150000);
        pasta.setAvailable(true);
        pasta.setDescription("Linguine tossed with fresh shrimp, squid, and a rich tomato basil sauce.");
        pasta.setStations(java.util.List.of(StationType.GENERAL));

        MenuItemEntity salmon = new MenuItemEntity();
        salmon.setName("Pan-Seared Salmon");
        salmon.setDishCategory(DishCategory.MAIN_COURSE);
        salmon.setEstimatedPrepMinutes(14);
        salmon.setPrice(220000);
        salmon.setAvailable(true);
        salmon.setDescription("Fresh Norwegian salmon fillet with a lemon butter glaze and asparagus.");
        salmon.setStations(java.util.List.of(StationType.GRILL));

        MenuItemEntity makima = new MenuItemEntity();
        makima.setName("Makima");
        makima.setDishCategory(DishCategory.MAIN_COURSE);
        makima.setEstimatedPrepMinutes(30);
        makima.setPrice(1000000);
        makima.setAvailable(true);
        makima.setDescription("Chainsaw's special.");
        makima.setStations(java.util.List.of(StationType.GRILL));

        MenuItemEntity cheesecake = new MenuItemEntity();
        cheesecake.setName("New York Cheesecake");
        cheesecake.setDishCategory(DishCategory.DESSERT);
        cheesecake.setEstimatedPrepMinutes(3); // Fast prep, just plating
        cheesecake.setPrice(65000);
        cheesecake.setAvailable(true);
        cheesecake.setDescription("Classic creamy cheesecake with a graham cracker crust and berry compote.");
        cheesecake.setStations(java.util.List.of(StationType.DESSERT));

        MenuItemEntity coffee = new MenuItemEntity();
        coffee.setName("Iced Caramel Macchiato");
        coffee.setDishCategory(DishCategory.BEVERAGE);
        coffee.setEstimatedPrepMinutes(4);
        coffee.setPrice(55000);
        coffee.setAvailable(true);
        coffee.setDescription("Rich espresso poured over cold milk, ice, and caramel syrup.");
        coffee.setStations(java.util.List.of(StationType.BEVERAGE));

        MenuItemEntity beer = new MenuItemEntity();
        beer.setName("Local Craft IPA");
        beer.setDishCategory(DishCategory.BEVERAGE);
        beer.setEstimatedPrepMinutes(1);
        beer.setPrice(75000);
        beer.setAvailable(true);
        beer.setDescription("Refreshing India Pale Ale with citrus and pine notes.");
        beer.setStations(java.util.List.of(StationType.BEVERAGE));

        // Group them all up and save
        java.util.List<MenuItemEntity> seed = java.util.List.of(
            pizza1, pizza2, burger, fries, salad, cake, drink, 
            wings, garlicBread, steak, pasta, salmon, makima, cheesecake, coffee, beer
        );
        
        menuRepository.saveAll(seed);
    }

    private void createManagerIfNotExists() {
        if (userRepository.existsByEmail("manager@irms.com")) return;
    
        UserEntity manager = new UserEntity();

        manager.setName("System Manager");
        manager.setEmail("manager@irms.com");
        manager.setRole(RoleType.MANAGER);
      
        manager.setHashedPassword(passwordEncoder.encode("manager123"));

        userRepository.save(manager);
    }
    private void createChefIfNotExists() {
        if (userRepository.existsByEmail("chef@irms.com")) return;
    
        UserEntity chef = new UserEntity();
        chef.setName("System Chef");
        chef.setEmail("chef@irms.com");
        chef.setRole(RoleType.CHEF);
        chef.setHashedPassword(passwordEncoder.encode("chef123"));

        userRepository.save(chef);
    }

    private void createServerIfNotExists() {
        if (userRepository.existsByEmail("server@irms.com")) return;
    
        UserEntity server = new UserEntity();
        server.setName("System Server");
        server.setEmail("server@irms.com");
        server.setRole(RoleType.SERVER);
      
        server.setHashedPassword(passwordEncoder.encode("server123"));

        userRepository.save(server);
    }

    private void createCashierIfNotExists() {
        if (userRepository.existsByEmail("cashier@irms.com")) return;
    
        UserEntity cashier = new UserEntity();
        cashier.setName("System Cashier");
        cashier.setEmail("cashier@irms.com");
        cashier.setRole(RoleType.CASHIER);
      
        cashier.setHashedPassword(passwordEncoder.encode("cashier123"));

        userRepository.save(cashier);
    }
}
