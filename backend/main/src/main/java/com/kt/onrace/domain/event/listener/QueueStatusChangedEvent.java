package com.kt.onrace.domain.event.listener;

// record는 자동으로 equals, hashcode 등 메소드를 지원해줌, Spring Event로 사용하기 위해 상속이나 인터페이스 불필요 하다
// Spring 4.2부터 POJO 객체도 이벤트로 사용 가능
public record QueueStatusChangedEvent() {
}
