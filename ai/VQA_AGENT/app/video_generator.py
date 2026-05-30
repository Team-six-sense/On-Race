import cv2
import numpy as np
import random
import uuid
import os
import math

def draw_secure_text(frame, text, pos, frame_count, font_scale=1.1, color=(255, 255, 255), is_static=False):
    """보안 강화 텍스트 렌더링 (지터 및 폰트 랜덤화)"""
    fonts = [cv2.FONT_HERSHEY_SIMPLEX, cv2.FONT_HERSHEY_DUPLEX, cv2.FONT_HERSHEY_COMPLEX, cv2.FONT_HERSHEY_TRIPLEX]
    
    if is_static:
        off_x = random.randint(-1, 1) if frame_count % 5 == 0 else 0
        off_y = random.randint(-1, 1) if frame_count % 5 == 0 else 0
        jitter_scale = font_scale + (math.sin(frame_count * 0.2) * 0.01)
        current_font = fonts[(frame_count // 5) % len(fonts)]
    else:
        off_x, off_y = random.choice([0, 0, 1]), 0
        jitter_scale, current_font = font_scale, cv2.FONT_HERSHEY_SIMPLEX

    new_pos = (pos[0] + off_x, pos[1] + off_y)
    bg_jitter = 65 + random.randint(-8, 8) 
    cv2.putText(frame, text, new_pos, current_font, jitter_scale, (bg_jitter, bg_jitter, bg_jitter), 7, cv2.LINE_AA)
    cv2.putText(frame, text, new_pos, current_font, jitter_scale, color, 2, cv2.LINE_AA)

def add_static_noise(roi):
    """결과창 배경에 노이즈 추가"""
    h, w = roi.shape[:2]
    n_pixels = int(h * w * 0.005)
    for _ in range(n_pixels):
        ry, rx = random.randint(0, h-1), random.randint(0, w-1)
        roi[ry, rx] = [random.randint(150, 200)] * 3
    return roi

def overlay_image_perfect(background, overlay, x, y, size=(90, 90), frame_count=0):
    """PNG 알파 채널을 활용한 캐릭터 합성"""
    overlay = cv2.resize(overlay, size)
    h, w = overlay.shape[:2]
    if x + w > background.shape[1] or y + h > background.shape[0] or x < 0 or y < 0:
        return background
    alpha = overlay[:, :, 3] / 255.0
    for c in range(3):
        background[y:y+h, x:x+w, c] = ((1 - alpha) * background[y:y+h, x:x+w, c] + alpha * overlay[:, :, c])
    return background

def generate_marathon_params(num_runners=4):
    """경주 기본 설정 (배번만 생성)"""
    return {
        "id": str(uuid.uuid4()), 
        "runners": random.sample([str(i) for i in range(1000, 10000)], num_runners)
    }
    
    
def generate_marathon_video_logic(params, video_path):
    # 이미지 불러오기
    char_imgs = [cv2.imread('static/run4.png', cv2.IMREAD_UNCHANGED), cv2.imread('static/run3.png', cv2.IMREAD_UNCHANGED)]
    boost_img = cv2.imread('static/Speed_Up.png', cv2.IMREAD_UNCHANGED)
    slow_img = cv2.imread('static/Speed_Down.png', cv2.IMREAD_UNCHANGED)

    for i in range(len(char_imgs)):
        if char_imgs[i] is None:
            char_imgs[i] = np.zeros((100, 100, 4), dtype=np.uint8)
            char_imgs[i][:] = [0, 120, 255, 255]

    width, height, fps, total_frames = 1280, 720, 20, 240
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(video_path, fourcc, fps, (width, height))

    # 기본 속도 및 위치
    num_runners = len(params['runners'])
    base_speeds = [random.uniform(9, 14) for _ in range(num_runners)]
    speeds, runners_x = base_speeds[:], [random.randint(-250, -150) for _ in range(num_runners)]
    finish_times = [None] * num_runners
    finish_line_x, stop_line_x, saved_display_time = 1050, 1120, ""
    

    # 각 러너의 효과 지속 프레임
    # 0이면 기본 상태
    effect_until = [0] * num_runners
    effect_type = [None] * num_runners  # "slow" or "boost"

    # 장애물 빈도 줄임: 2~3개
    # 1. 모든 레인 번호를 2번씩 넣은 리스트 (최대 2개 허용)
    lane_pool = []
    for i in range(num_runners):
        lane_pool.extend([i, i]) 

    # 2. 전체 장애물 개수 결정 (2~3개)
    num_obstacles = random.randint(2, 3)

    # 3. 중복 없이 추출
    selected_lanes = random.sample(lane_pool, num_obstacles)

    obstacles = []
    # 레인별로 이미 배치된 X 좌표를 저장
    lane_x_map = {} 
    min_dist = 180 # 캐릭터 크기를 고려한 최소 간격

    for lane_idx in selected_lanes:
        if lane_idx not in lane_x_map:
            # 해당 레인 첫 번째 장애물: 전체 범위(400~850)에서 자유롭게 생성
            new_x = random.randint(400, 850)
            lane_x_map[lane_idx] = new_x
        else:
            # 해당 레인 두 번째 장애물: 첫 번째 위치(prev_x)를 피해서 생성
            prev_x = lane_x_map[lane_idx]
            new_x = random.randint(400, 850)
            
            # 선택 가능한 두 영역: [400 ~ prev_x - min_dist] 또는 [prev_x + min_dist ~ 850]
            valid_ranges = []
            if prev_x - min_dist >= 400:
                valid_ranges.append((400, prev_x - min_dist))
            if prev_x + min_dist <= 850:
                valid_ranges.append((prev_x + min_dist, 850))
            
            # 가능한 영역이 있다면 그중 하나를 골라 생성
            if valid_ranges:
                chosen_range = random.choice(valid_ranges)
                new_x = random.randint(chosen_range[0], chosen_range[1])
            else:
                # 만약 공간이 너무 안 나오면(드문 경우), 그냥 최대한 멀리 떨어뜨림
                new_x = 400 if prev_x > 625 else 850

        obstacles.append({
            "x": new_x,
            "lane": lane_idx,
            "type": random.choice(["slow", "boost"]),
            "hit": False,
            "hit_bib": None
        })

    # 배경 건물
    buildings = []
    for x in range(0, width, 100):
        bh = random.randint(100, 250)
        bw = random.randint(80, 150)
        bcol = (random.randint(70, 100), random.randint(70, 100), random.randint(70, 100))
        buildings.append((x, bh, bw, bcol))

    skyline_y, runner_w, obs_w  = int(height * 0.28), 130, 40

    for f in range(total_frames):
        all_finished = all(t is not None for t in finish_times)
        display_time = f"02:15:{10 + (f/fps):05.2f}" if not all_finished else saved_time
        if not all_finished: saved_time = display_time

        # 1. 하늘 배경
        frame = np.full((height, width, 3), (245, 220, 180), dtype=np.uint8)

        # 2. 건물 스카이라인
        for bx, bh, bw, bcol in buildings:
            cv2.rectangle(frame, (bx, skyline_y - bh), (bx + bw, skyline_y), bcol, -1)
            for wx in range(bx + 15, bx + bw - 15, 30):
                for wy in range(skyline_y - bh + 20, skyline_y - 15, 40):
                    cv2.rectangle(frame, (wx, wy), (wx + 10, wy + 15), (210, 210, 210), -1)

        # 3. 관중석
        cv2.rectangle(frame, (0, skyline_y - 50), (width, skyline_y), (40, 40, 40), -1)
        for i in range(0, width, 25):
            p_color = (
                random.randint(60, 220),
                random.randint(60, 220),
                random.randint(60, 220)
            )
            cv2.circle(frame, (i + 12, skyline_y - 35), 10, p_color, -1)
            cv2.line(frame, (i + 12, skyline_y - 25), (i + 12, skyline_y), p_color, 4)

        # 4. 도로
        frame[skyline_y:height, :] = [65, 65, 65]

        # 레인 계산 
        top_margin = 80    # 펜스 아래 여백
        bottom_margin = 50 # 화면 맨 아래 여백
        
        lane_start_y = skyline_y + top_margin
        available_h = height - lane_start_y - bottom_margin
        lane_h = available_h // 4 

        # 1. 레인 구분선 그리기 (총 5개의 선)
        for i in range(5): 
            line_y = lane_start_y + (i * lane_h)
            cv2.line(frame, (0, line_y), (width, line_y), (100, 100, 100), 2)
            
            # 레인 번호 텍스트 (레인 중앙에 배치)
            if i < 4:
                text_y = line_y + (lane_h // 2) + 10
                cv2.putText(frame, f"LANE {i+1}", (40, text_y), 
                            cv2.FONT_HERSHEY_SIMPLEX, 0.7, (160, 160, 160), 2, cv2.LINE_AA)
        # 5. 전광판
        cv2.rectangle(frame, (width // 2 - 180, 40), (width // 2 + 180, 110), (0, 0, 0), -1)
        cv2.rectangle(frame, (width // 2 - 180, 40), (width // 2 + 180, 110), (180, 180, 180), 3)
        cv2.putText(
            frame, display_time, (width // 2 - 150, 90),
            cv2.FONT_HERSHEY_DUPLEX, 1.3, (50, 255, 255), 2, cv2.LINE_AA
        )

        # 7. 결승선
        cv2.rectangle(frame, (finish_line_x, skyline_y), (finish_line_x + 60, height), (255, 255, 255), -1)
        for check_y in range(skyline_y, height, 40):
            if (check_y // 40) % 2 == 0:
                cv2.rectangle(frame, (finish_line_x, check_y), (finish_line_x + 30, check_y + 20), (0, 0, 0), -1)
                cv2.rectangle(frame, (finish_line_x + 30, check_y + 20), (finish_line_x + 60, check_y + 40), (0, 0, 0), -1)

        # 8. 장애물 렌더링 
        reveal_dist = 350
        boost_size = (50, 50)
        slow_size = (50, 50)

        for obs in obstacles:
            # 해당 레인의 러너 현재 위치 가져오기
            current_runner_x = runners_x[obs["lane"]]
            dist_to_obs = obs["x"] - current_runner_x

            # [핵심] 장애물을 아직 밟지 않았고, 지정된 거리 안에 있을 때만 그리기
            if not obs["hit"] and 0 < dist_to_obs < reveal_dist:
                # 장애물 타입 설정
                target_img = boost_img if obs["type"] == "boost" else slow_img
                current_size = boost_size if obs["type"] == "boost" else slow_size
                
                # Y 좌표 계산
                obs_y = int(lane_start_y + (obs["lane"] * lane_h) + (lane_h // 2) - (current_size[1] // 2))
                obs_x = int(obs["x"])

                # 리사이징 및 합성
                temp_obs = cv2.resize(target_img, current_size, interpolation=cv2.INTER_AREA)
                
                if 0 <= obs_x < width - current_size[0] and 0 <= obs_y < height - current_size[1]:
                    # 서서히 나타나는 효과 (거리에 따른 알파값 조절)
                    alpha_factor = min(1.0, (reveal_dist - dist_to_obs) / 100)
                    overlay_alpha = (temp_obs[:, :, 3] / 255.0) * alpha_factor

                    for c in range(3):
                        roi = frame[obs_y:obs_y+current_size[1], obs_x:obs_x+current_size[0], c]
                        frame[obs_y:obs_y+current_size[1], obs_x:obs_x+current_size[0], c] = (
                            (1.0 - overlay_alpha) * roi + overlay_alpha * temp_obs[:, :, c]
                        ).astype(np.uint8)

        # 9. 주자 로직
        for i in range(num_runners):
            # 효과 지속 시간 체크
            if effect_until[i] > 0 and f >= effect_until[i]:
                speeds[i] = base_speeds[i]
                effect_until[i] = 0
                effect_type[i] = None

            runner_left = runners_x[i]
            runner_right = runners_x[i] + runner_w

            # 충돌 체크: 같은 레인 + 실제 겹침
            for obs in obstacles:
                # [수정] 장애물 너비를 고려한 정확한 충돌 판정
                obs_left = obs["x"]
                obs_right = obs["x"] + 50 # current_size[0]

                if (
                    not obs["hit"]
                    and obs["lane"] == i
                    and runner_right > obs_left
                    and runner_left < obs_right
                ):
                    obs["hit"] = True
                    # [추가] 누가 밟았는지 배번 기록 (질문 생성용)
                    obs["hit_bib"] = params['runners'][i] 

                    if obs["type"] == "slow":
                        speeds[i] = max(base_speeds[i] * 0.45, 4.0)
                        runners_x[i] -= 8 # 순간적인 밀림 효과
                        effect_until[i] = f + 18 # 약 0.9초 지속
                        effect_type[i] = "slow"
                    else:
                        speeds[i] = min(base_speeds[i] * 1.7, 20.0)
                        runners_x[i] += 12 # 순간적인 가속 효과
                        effect_until[i] = f + 18
                        effect_type[i] = "boost"

            # 이동 및 완주 처리
            if runners_x[i] < stop_line_x:
                runners_x[i] += speeds[i]
                if (runners_x[i] + 110) >= finish_line_x and finish_times[i] is None:
                    finish_times[i] = display_time
            else:
                runners_x[i] = stop_line_x

            # 캐릭터 렌더링 (동일)
            img_idx = (f // 3) % 2
            frame = overlay_image_perfect(
                frame, char_imgs[img_idx], int(runners_x[i]),
                skyline_y + 50 + i * 95, size=(140, 140), frame_count=f
            )

            # [보안] 효과 적용 시 배번 색상 변경 (VQA 힌트)
            bib_color = (255, 255, 255)
            if effect_type[i] == "slow":
                bib_color = (100, 100, 255)  # 감속 시 파란색 계열
            elif effect_type[i] == "boost":
                bib_color = (100, 255, 100)  # 가속 시 초록색 계열

            draw_secure_text(
                frame, params['runners'][i],
                (int(runners_x[i]) + 25, skyline_y + 45 + i * 95),
                f, color=bib_color
            )

        # 10. 최종 결과 전광판 
        if all_finished:
            overlay = frame.copy()
            board_y_start = 210 
            cv2.rectangle(overlay, (width // 2 - 300, board_y_start), (width // 2 + 300, board_y_start + 450), (0, 0, 0), -1)
            cv2.addWeighted(overlay, 0.7, frame, 0.3, 0, frame)

            roi = frame[board_y_start: board_y_start + 450, width // 2 - 300: width // 2 + 300]
            add_static_noise(roi)

            cv2.putText(
                frame, "OFFICIAL RESULTS",
                (width // 2 - 120, board_y_start + 50),
                cv2.FONT_HERSHEY_DUPLEX, 1.2, (50, 255, 255), 2
            )

            # --- 결과 데이터에 레인 정보 매칭 ---
            # 이미 정렬된 결과를 출력할 때, 해당 배번이 몇 번 레인이었는지 찾아서 표시합니다.
            valid_results = []
            for idx, f_time in enumerate(finish_times):
                valid_results.append({
                    "lane": idx + 1,
                    "bib": params['runners'][idx],
                    "time": f_time
                })
            
            # 시간 순으로 정렬 (1등부터 순서대로)
            valid_results.sort(key=lambda x: x['time'])

            for idx, res in enumerate(valid_results):
                y_offset = board_y_start + 160 + (idx * 70) # 간격을 조금 더 벌림

                # 1. 등수 표시 (RANK 1)
                draw_secure_text(
                    frame, f"{idx + 1}ST" if idx==0 else f"{idx+1}ND" if idx==1 else f"{idx+1}RD" if idx==2 else f"{idx+1}TH",
                    (width // 2 - 260, y_offset), f, font_scale=0.9, color=(200, 200, 200), is_static=True
                )

                # 2. 레인 정보 표시 (LANE X) - 새로 추가된 부분!
                draw_secure_text(
                    frame, f"LANE {res['lane']}",
                    (width // 2 - 120, y_offset), f, font_scale=0.9, color=(50, 255, 255), is_static=True
                )

                # 3. 배번 및 기록 표시 (BIB: XXXX / 02:15:XX)
                draw_secure_text(
                    frame, f"{res['time']}",
                    (width // 2 + 60, y_offset), f, font_scale=0.9, color=(255, 255, 255), is_static=True
                )

        # 11. 하단 펜스
        cv2.rectangle(frame, (0, skyline_y - 15), (width, skyline_y), (150, 150, 150), -1)
        for fence_x in range(0, width, 30):
            cv2.line(frame, (fence_x, skyline_y - 15), (fence_x, skyline_y + 15), (100, 100, 100), 2)

        out.write(frame)

    out.release()
    
    # 최종 데이터 반환
    slow_hit_bibs = []
    boost_hit_bibs = []

    for obs in obstacles:
        if obs["hit"] and obs["hit_bib"] is not None:
            if obs["type"] == "slow":
                slow_hit_bibs.append(obs["hit_bib"])
            elif obs["type"] == "boost":
                boost_hit_bibs.append(obs["hit_bib"])

    # 중복 제거
    slow_hit_bibs = list(set(slow_hit_bibs))
    boost_hit_bibs = list(set(boost_hit_bibs))

    # 2. 각 주자별 상세 결과 생성
    results = []
    # 주자들의 배번 리스트 순서대로 결과 구성
    for i in range(num_runners):
        bib = params['runners'][i]
        
        # 해당 주자가 장애물을 밟았는지 여부 판단
        is_hit_slow = bib in slow_hit_bibs
        is_hit_boost = bib in boost_hit_bibs
        
        results.append({
            "bib": bib,
            "time": finish_times[i],
            "lane": i + 1,
            "hit_slow": is_hit_slow,   # 이제 정상적으로 True/False가 찍힙니다.
            "hit_boost": is_hit_boost,
            "hit_any": is_hit_slow or is_hit_boost
        })

    # 4. 시간 순으로 정렬하여 등수(rank) 부여
    results.sort(key=lambda x: x['time'])
    for idx, r in enumerate(results):
        r['rank'] = idx + 1

    # 5. 질문 생성기에서 사용할 수 있도록 리스트 반환
    return results