package com.example.jwttest.global.batch;

import org.springframework.batch.core.ItemReadListener;

public class DelayItemReadListener<T> implements ItemReadListener<T> {
    private static final long DEFAULT_DELAY_MILLIS = 100;
    private final long delayMillis; // 지연 시간 (밀리초 단위)

    public DelayItemReadListener(long delayMillis) {
        this.delayMillis = delayMillis; // 생성자에서 지연 시간을 전달받음
    }

    public DelayItemReadListener() {
        this.delayMillis = DEFAULT_DELAY_MILLIS;
        // 인자가 없는 경우 미리 정의한 기본 값을 사용
    }

    @Override
    public void afterRead(T item) {
        try {
            Thread.sleep(delayMillis); // 전달받은 지연 시간만큼 스레드를 일시 중지
        } catch (InterruptedException e) { // 인터럽트 되어 InterruptedException 예외가 발생했다면
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태를 설정
        }
    }

    /**
     * 몰라서 bing에 물어본 내용
     *
     * 인터럽트는 스레드에게 현재 작업을 중단하고 다른 작업을 수행하라는 지시입니다.
     * 스레드가 인터럽트에 어떻게 반응할지는 프로그래머가 결정해야 합니다. 하지만 일반적으로 스레드가 종료되는 것이 매우 일반적입니다.
     *
     * Thread.interrupt() 메서드는 대상 스레드의 인터럽트 상태/플래그를 설정합니다.
     * 그런 다음 대상 스레드에서 실행중인 코드가 인터럽트 상태를 폴링하고 적절하게 처리할 수 있습니다.
     * Object.wait()와 같은 일부 차단 메서드는 인터럽트 상태를 즉시 소비하고 적절한 예외(일반적으로 InterruptedException)를 발생시킬 수 있습니다.
     *
     * Java에서 인터럽트는 선점적이지 않습니다. 다시 말해 두 스레드 모두 인터럽트를 적절하게 처리하기 위해 협력해야 합니다.
     * 대상 스레드가 인터럽트 상태를 폴링하지 않으면 인터럽트가 실질적으로 무시됩니다. 폴링은 Thread.interrupted() 메서드를 통해 이루어지며,
     * 이 메서드는 현재 스레드의 인터럽트 상태를 반환하고 해당 인터럽트 플래그를 지웁니다.
     *
     * InterruptedException은 스레드가 대기, 수면 또는 다른 작업 중에 인터럽트될 때 발생하는 예외입니다.
     * 즉, 어떤 코드가 스레드의 interrupt() 메서드를 호출했습니다.
     * 이것은 검사 예외이며 Java의 많은 차단 작업에서 발생할 수 있습니다.
     */
}
