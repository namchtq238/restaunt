package gogitek.restaurentorder.controller.admincontroller;

import gogitek.restaurentorder.constaint.FormatPrice;
import gogitek.restaurentorder.entity.Product;
import gogitek.restaurentorder.service.AdminService;
import gogitek.restaurentorder.service.CategoryService;
import gogitek.restaurentorder.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Controller
public class ProductAdminController {
    private static final String currentDirectory = System.getProperty("user.dir");
    private static final Path path = Paths.get(currentDirectory + Paths.get("/target/classes/static/image"));
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final FormatPrice formatPrice;
    private final ProductService productService;

    public ProductAdminController(AdminService adminService, CategoryService categoryService,
                                  FormatPrice formatPrice, ProductService productService) {
        this.adminService = adminService;
        this.categoryService = categoryService;
        this.formatPrice = formatPrice;
        this.productService = productService;
    }

    @ModelAttribute
    public void addFormatService(Model model) {
        model.addAttribute("format", formatPrice);
    }

    @GetMapping("/admin/product")
    public String getViewProductAdmin(Model model) {
        model.addAttribute("dsProduct", adminService.getListProduct());
        return "admin-page/product";
    }

    @GetMapping("/admin/product/add")
    public String getViewAddProductAdmin(Model model) {
        model.addAttribute("categoryList", categoryService.getListCategory());
        model.addAttribute("product", new Product());
        return "admin-page/add-product";
    }

    @PostMapping("/admin/product/add")
    public String handleAddProduct(@ModelAttribute @Valid Product product,
                                   @RequestParam MultipartFile photo,
                                   BindingResult result) {
        if (photo.isEmpty() || result.hasErrors()) return "redirect:/admin/product/add";
        if (!photo.isEmpty()) {
            try {
                Path fileNameAndPath = Paths.get(String.valueOf(path), photo.getOriginalFilename());
                Files.write(fileNameAndPath, photo.getBytes());
                product.setImage(photo.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/edit/{id}")
    public String getViewEditProductAdmin(@PathVariable("id") Long productId, Model model) {
        model.addAttribute("categoryList", categoryService.getListCategory());
        model.addAttribute("product", productService.getProductById(productId));
        return "admin-page/add-product";
    }

    @PostMapping("/admin/product/edit/{id}")
    public String handleEditProductAdmin(@PathVariable("id") Long productId, @ModelAttribute Product product,
                                         @RequestParam MultipartFile photo, BindingResult result) {
        if (result.hasErrors()) return "redirect:/admin/product/edit/" + productId;
        if (!photo.isEmpty()) {
            try {
                InputStream inputStream = photo.getInputStream();
                Files.copy(inputStream, path.resolve(photo.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
                product.setImage(photo.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.updateProduct(productId, product);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String handleDeleteProductAdmin(@PathVariable("id") Long productId) {
        productService.deleteProduct(productId);
        return "redirect:/admin/product";
    }

}
