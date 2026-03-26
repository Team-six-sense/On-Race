package com.kt.onrace.auth.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.entity.TermMaster;
import com.kt.onrace.auth.entity.TermVersion;
import com.kt.onrace.auth.repository.TermMasterRepository;
import com.kt.onrace.auth.repository.TermVersionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TermDataInitializer implements ApplicationRunner {

	private final TermMasterRepository termMasterRepository;
	private final TermVersionRepository termVersionRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		initTerm("이용약관", true,
			"On-Race 서비스 이용약관 (v1.0)\n\n본 약관은 On-Race 서비스의 이용 조건 및 절차에 관한 사항을 규정합니다.");
		initTerm("개인정보처리방침", true,
			"On-Race 개인정보처리방침 (v1.0)\n\n수집하는 개인정보의 항목, 수집 및 이용목적, 보유 및 이용기간 등을 안내합니다.");
		initTerm("마케팅 수신 동의", false,
			"On-Race 마케팅 수신 동의 (v1.0)\n\n이벤트, 혜택 등 마케팅 정보를 이메일 및 SMS로 수신하는 것에 동의합니다.");

		log.info("[TermDataInitializer] 약관 시드 데이터 초기화 완료");
	}

	private void initTerm(String name, boolean required, String content) {
		if (termMasterRepository.existsByName(name)) {
			log.debug("[TermDataInitializer] '{}' 약관이 이미 존재합니다. 스킵합니다.", name);
			return;
		}
		TermMaster master = termMasterRepository.save(TermMaster.create(name, required));
		termVersionRepository.save(TermVersion.create(master, "1.0", content, true));
		log.info("[TermDataInitializer] '{}' 약관 삽입 완료", name);
	}
}
