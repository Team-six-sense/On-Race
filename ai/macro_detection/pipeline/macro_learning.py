import json
import os
import math
import pandas as pd
import numpy as np
import xgboost as xgb
from config import TRAIN_DATA_PATH, MODEL_FILE

def extract_advanced_features(raw_data_list):
    
    if len(raw_data_list) < 5:
        return None

    raw_data_list.sort(key=lambda item: item['timestamp'])

    total_distance = 0.0
    pause_count = 0
    down_count = 0
    move_count = 0
    
    click_durations = []
    last_down_time = None
    
    velocities = []
    accelerations = [] 
    jerks = []         
    angle_changes = []
    
    last_velocity = None
    last_accel = None
    last_angle = None
    jitter_count = 0   

    first_pt = raw_data_list[0]
    last_pt = raw_data_list[-1]

    for i in range(len(raw_data_list)):
        current = raw_data_list[i]
        e_type = current.get('eventType')

        if e_type == 'pointerdown':
            down_count += 1
            last_down_time = current['timestamp']
        elif e_type == 'pointerup':
            if last_down_time is not None:
                duration = current['timestamp'] - last_down_time
                if duration > 0:
                    click_durations.append(duration)
                last_down_time = None
        elif e_type == 'pointermove':
            move_count += 1

        if i > 0:
            prev = raw_data_list[i-1]
            dt = current['timestamp'] - prev['timestamp'] + 1e-5
            dx = current['x'] - prev['x']
            dy = current['y'] - prev['y']

            if dt > 150:
                pause_count += 1

            dist = math.hypot(dx, dy) 
            total_distance += dist
            
            # 1. 속도
            v = dist / dt
            velocities.append(v)
            
            # 2. 가속도 & 가가속도(Jerk)
            if last_velocity is not None:
                a = (v - last_velocity) / dt
                accelerations.append(a)
                
                if last_accel is not None:
                    j = (a - last_accel) / dt
                    jerks.append(j)
                last_accel = a
            last_velocity = v

            # 3. 각도 및 손떨림(Jitter)
            angle = math.atan2(dy, dx)
            if last_angle is not None:
                delta_angle = angle - last_angle
                delta_angle = (delta_angle + math.pi) % (2 * math.pi) - math.pi
                angle_changes.append(delta_angle)
                
                if len(angle_changes) > 1:
                    if angle_changes[-1] * angle_changes[-2] < 0:
                        jitter_count += 1
                        
            last_angle = angle

    # 표준편차 계산 함수
    def calc_std(lst, mean_val):
        if len(lst) < 2: return 0.0
        variance = sum((x - mean_val) ** 2 for x in lst) / (len(lst) - 1)
        return math.sqrt(variance)

    # 기본 통계
    v_mean = sum(velocities) / len(velocities) if velocities else 0.0
    v_std = calc_std(velocities, v_mean)
    ac_mean = sum(angle_changes) / len(angle_changes) if angle_changes else 0.0
    ac_std = calc_std(angle_changes, ac_mean)
    
    # 클릭 통계
    avg_click = sum(click_durations) / len(click_durations) if click_durations else 0.0
    std_click = calc_std(click_durations, avg_click)
    move_to_click_ratio = move_count / (down_count + 1e-5)

    # 고급 통계 (Jerk, Path Efficiency, Jitter)
    j_mean = sum(jerks) / len(jerks) if jerks else 0.0
    j_std = calc_std(jerks, j_mean)
    
    # 경로 효율성 계산
    straight_dist = math.hypot(last_pt['x'] - first_pt['x'], last_pt['y'] - first_pt['y'])
    path_efficiency = total_distance / (straight_dist + 1e-5)
    
    # 손떨림 비율 계산
    jitter_rate = jitter_count / (move_count + 1e-5)

    # Feature 항목
    features = {
        'velocity_mean': float(v_mean),
        'velocity_std': float(v_std),
        'jerk_mean': float(j_mean),
        'jerk_std': float(j_std),
        'angle_change_std': float(ac_std),
        'jitter_rate': float(jitter_rate),
        'path_efficiency': float(path_efficiency),
        'total_distance': float(total_distance),
        'pause_count': int(pause_count),
        'move_to_click_ratio': float(move_to_click_ratio),
        'total_clicks': int(down_count),
        'avg_click_duration': float(avg_click),
        'std_click_duration': float(std_click)
    }
    
    return features

def process_training_data(json_file_path):
    print(f"[{json_file_path}] 전처리 시작...")
    
    features_list = []
    labels = []
    
    with open(json_file_path, 'r', encoding='utf-8') as f:
        data_list = json.load(f) # JSON 배열 통째로 읽기
        
        for idx, session in enumerate(data_list):
            try:
                is_macro = session.get('is_macro')
                points = session.get('points', [])
                
                if is_macro is None or len(points) < 5:
                    continue

                features = extract_advanced_features(points)
                if features is not None:
                    features_list.append(features)
                    labels.append(is_macro)
                    
            except Exception as e:
                print(f"[{idx}번째 데이터] 파싱 에러 (무시됨): {e}")

    X_train = pd.DataFrame(features_list).fillna(0)
    y_train = np.array(labels)
    
    print(f"{len(X_train)}개의 유효한 세션 추출 완료.")
    return X_train, y_train

if __name__ == "__main__":

    X_train, y_train = process_training_data(TRAIN_DATA_PATH)

    if len(X_train) == 0:
        print("학습을 진행할 유효한 데이터 부족")
        exit()

    unique_classes = np.unique(y_train)
    if len(unique_classes) < 2:
        print(f"정답지가 한 종류({unique_classes[0]})밖에 없어 비교 학습 불가.")
        exit()

    model = xgb.XGBClassifier(
        n_estimators=100, 
        learning_rate=0.05, 
        max_depth=5, 
        random_state=42
    )

    if os.path.exists(MODEL_FILE):
        print(f"{MODEL_FILE} 파일이 존재합니다. 기존 모델을 불러와서 고도화 학습을 진행합니다...")
        model.load_model(MODEL_FILE)
        model.fit(X_train, y_train, xgb_model=MODEL_FILE)
    else:
        print(f"{MODEL_FILE} 파일이 존재하지 않습니다. 새로운 고도화 학습을 시작합니다...")
        model.fit(X_train, y_train)

    model.save_model(MODEL_FILE)
    print(f"{MODEL_FILE} 저장 완료")