package com.greenchain.backend.controller;

import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.SupplierRepository;
import com.greenchain.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @GetMapping("/{id}")
    public Supplier getSupplierById(@PathVariable Long id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Only admin can create any supplier, supplier user can only create supplier
        // for themselves
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.SUPPLIER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // If user is supplier, they can only create supplier for themselves
        if (currentUser.getRole() == User.Role.SUPPLIER) {
            // Check if user already has a supplier
            Supplier existingSupplier = supplierRepository.findByUserUsername(currentUser.getUsername()).orElse(null);
            if (existingSupplier != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Set the user for the supplier
            supplier.setUser(currentUser);
        }

        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Supplier existingSupplier = supplierRepository.findById(id).orElse(null);
        if (existingSupplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if user is admin or the supplier's own user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null ||
                (currentUser.getRole() != User.Role.ADMIN &&
                        (existingSupplier.getUser() == null
                                || existingSupplier.getUser().getId() != currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update only the fields that are provided, preserving the existing values
        if (supplier.getName() != null) {
            existingSupplier.setName(supplier.getName());
        }
        if (supplier.getCountry() != null) {
            existingSupplier.setCountry(supplier.getCountry());
        }
        if (supplier.getHasEnvironmentalCertification() != null) {
            existingSupplier.setHasEnvironmentalCertification(supplier.getHasEnvironmentalCertification());
        }
        if (supplier.getEmissionFactorPerUnit() != null) {
            existingSupplier.setEmissionFactorPerUnit(supplier.getEmissionFactorPerUnit());
        }
        if (supplier.getContactEmail() != null) {
            existingSupplier.setContactEmail(supplier.getContactEmail());
        }

        return ResponseEntity.ok(supplierRepository.save(existingSupplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Supplier existingSupplier = supplierRepository.findById(id).orElse(null);
        if (existingSupplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if user is admin or the supplier's own user
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null ||
                (currentUser.getRole() != User.Role.ADMIN &&
                        (existingSupplier.getUser() == null
                                || existingSupplier.getUser().getId() != currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/certified")
    public List<Supplier> getCertifiedSuppliers() {
        return supplierRepository.findByHasEnvironmentalCertificationTrue();
    }

    @GetMapping("/me")
    public ResponseEntity<Supplier> getCurrentSupplier(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        Supplier supplier = supplierRepository.findByUserUsername(username).orElse(null);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Supplier supplier = supplierRepository.findByUserUsername(username).orElse(null);
        return ResponseEntity.ok("User: " + user + "\nSupplier: " + supplier);
    }

    @GetMapping("/link-user")
    public ResponseEntity<?> linkUserToSupplier(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Find supplier by name (assuming company name matches supplier name)
        Supplier supplier = supplierRepository.findAll().stream()
                .filter(s -> s.getName() != null && s.getName().equals(user.getCompanyName()))
                .findFirst()
                .orElse(null);

        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Supplier not found");
        }

        // Link user to supplier
        supplier.setUser(user);
        supplierRepository.save(supplier);

        return ResponseEntity.ok("Linked user " + username + " to supplier " + supplier.getName());
    }

    @PostMapping("/link-user-manual")
    public ResponseEntity<?> linkUserToSupplierManual(@RequestParam Long supplierId, @RequestParam String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);

        if (user == null || supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or supplier not found");
        }

        // Link user to supplier
        supplier.setUser(user);
        supplierRepository.save(supplier);

        return ResponseEntity.ok("Linked user " + username + " to supplier " + supplierId);
    }

    @PutMapping("/me")
    public ResponseEntity<Supplier> updateCurrentSupplier(@RequestBody Supplier supplierData, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        Supplier supplier = supplierRepository.findByUserUsername(username).orElse(null);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (supplierData.getName() != null)
            supplier.setName(supplierData.getName());
        if (supplierData.getCountry() != null)
            supplier.setCountry(supplierData.getCountry());
        if (supplierData.getContactEmail() != null)
            supplier.setContactEmail(supplierData.getContactEmail());
        if (supplierData.getEmissionFactorPerUnit() != null)
            supplier.setEmissionFactorPerUnit(supplierData.getEmissionFactorPerUnit());
        if (supplierData.getHasEnvironmentalCertification() != null)
            supplier.setHasEnvironmentalCertification(supplierData.getHasEnvironmentalCertification());

        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    @PostMapping("/generate-users")
    public String generateSupplierUsers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        int created = 0;

        for (Supplier supplier : suppliers) {
            if (supplier.getUser() == null && supplier.getName() != null) {
                // Create user for supplier
                User user = new User();
                String username = supplier.getName().toLowerCase().replaceAll("\\s+", "_") + "_supplier";
                user.setUsername(username);
                user.setEmail(
                        supplier.getContactEmail() != null ? supplier.getContactEmail() : username + "@example.com");
                user.setPassword(passwordEncoder.encode("supplier123")); // Encoded password
                user.setRole(User.Role.SUPPLIER);
                user.setCompanyName(supplier.getName());
                user.setEnabled(true);

                user = userRepository.save(user);
                supplier.setUser(user);
                supplierRepository.save(supplier);
                created++;
            }
        }

        return "Created " + created + " user accounts for suppliers. Default password: supplier123";
    }
}