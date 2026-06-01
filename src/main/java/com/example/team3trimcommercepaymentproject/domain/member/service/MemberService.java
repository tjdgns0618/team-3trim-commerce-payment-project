package com.example.team3trimcommercepaymentproject.domain.member.service;

import org.springframework.stereotype.Service;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	public Member findMemberEntity(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(
			() -> new RuntimeException("존재하지 않는 회원입니다.")
		);
	}

}
