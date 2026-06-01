package com.example.team3trimcommercepaymentproject.domain.product.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductFilter;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductListGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ResponseEntity<ProductListGetResponse> getAllProduct(
		@RequestParam(required = false) String categoryName,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice,
		@RequestParam(required = false) Product.SaleStatus status,
		@PageableDefault(page = 1, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		int page = Math.max(pageable.getPageNumber() - 1, 0);
		int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
		pageable = PageRequest.of(page, size, pageable.getSort());

		ProductFilter productFilter = new ProductFilter(categoryName, minPrice, maxPrice, status);
		ProductListGetResponse response = productService.getAll(pageable, productFilter);

		return ResponseEntity.ok(response);
	}



}
