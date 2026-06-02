package com.example.team3trimcommercepaymentproject.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	public Member findMemberEntity(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(
			() -> new RuntimeException("존재하지 않는 회원입니다.")
		);
	}

	public Member addPoint(Long memberId, Long amount) {
		Member member = findMemberEntity(memberId);
		member.addPoint(amount);
		return member;
	}

	public Member usePoint(Long memberId, Long amount) {
		Member member = findMemberEntity(memberId);
		member.usePoint(amount);
		return member;
	}

}
