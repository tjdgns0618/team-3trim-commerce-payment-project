package com.example.team3trimcommercepaymentproject.domain.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductFilter;
import com.example.team3trimcommercepaymentproject.domain.product.dto.ProductListGetResponse;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public ProductListGetResponse getAll(Pageable pageable, ProductFilter filter) {
		Page<Product> productPage = productRepository.findAllByFilter(filter, pageable);

		return ProductListGetResponse.from(productPage);
	}

}
