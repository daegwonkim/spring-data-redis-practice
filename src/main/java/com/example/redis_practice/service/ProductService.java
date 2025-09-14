package com.example.redis_practice.service;

import com.example.redis_practice.annotation.DistributedLock;
import com.example.redis_practice.entity.Product;
import com.example.redis_practice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public void addProduct(Product product) {
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @DistributedLock(key = "'product:' + #id")
    public void decreaseStockWithDistributedLock(Long id){
        Product product = productRepository.findById(id).orElseThrow();
        if (product.getStock() > 0) {
            product.setStock(product.getStock() - 1);
            productRepository.save(product);
        }
    }

    public void decreaseStockWithoutDistributedLock(Long id){
        Product product = productRepository.findById(id).orElseThrow();
        if (product.getStock() > 0) {
            product.setStock(product.getStock() - 1);
            productRepository.save(product);
        }
    }
}
