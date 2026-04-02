import os
import math
import pandas as pd
import xgboost as xgb

from config import MODEL_FILE, MACRO_THRESHOLD

class MouseMacroDetector:
    def __init__(self, model_path=MODEL_FILE):
        # mk_model.bat로 생성된 모델 파일 로드
        if os.path.exists(model_path):
            self.model = xgb.XGBClassifier()
            self.model.load_model(model_path)
            print(f"모델 로드 성공 | '{model_path}'")
        else:
            print(f"모델 로드 실패 | '{model_path}'")
            raise FileNotFoundError(f"모델 파일이 없습니다: {model_path}")

    def _extract_features(self, raw_data_list):
        
        if len(raw_data_list) < 5:
            raise ValueError("분석하기에 이벤트 개수가 너무 적습니다 (최소 5개 필요).")

        raw_data_list.sort(key=lambda item: item['timestamp'])

        total_distance = 0.0    # 총 이동 거리
        pause_count = 0         # 멈춘 횟수
        down_count = 0          # 클릭 횟수
        move_count = 0          # 이동 이벤트 횟수
		
        click_durations = []    # 클릭 지속 시간 리스트
        last_down_time = None   # 마지막 클릭 다운 시간 저장용 변수
		
        velocities = []         # 속도 리스트
        accelerations = []      # 가속도 리스트
        jerks = []				# 가가속도 리스트		 
        angle_changes = []      # 각도 변화 리스트
		
        last_velocity = None    # 마지막 속도 저장용 변수
        last_accel = None       # 마지막 가속도 저장용 변수
        last_angle = None       # 마지막 각도 저장용 변수
        jitter_count = 0        # 손떨림 횟수 카운트

		# 분석을 위해 첫 번째와 마지막 이벤트 저장		 
        first_pt = raw_data_list[0]
        last_pt = raw_data_list[-1]				

        # x, y, timestamp, eventType 기준으로 Feature 추출
        for i in range(len(raw_data_list)):
            current = raw_data_list[i]
            e_type = current.get('eventType')

            # 이벤트 타입에 따른 카운트 및 클릭 지속 시간 계산
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
                # 이전 이벤트와의 시간 간격, 이동 거리, 각도 변화 등을 계산하여 Feature로 활용
                prev = raw_data_list[i-1]
                dt = current['timestamp'] - prev['timestamp'] + 1e-5
                dx = current['x'] - prev['x']
                dy = current['y'] - prev['y']

                # 1. 멈춘 횟수 및 이동 거리 계산
                if dt > 150:
                    pause_count += 1

                dist = math.hypot(dx, dy) 
                total_distance += dist
                
                v = dist / dt
                velocities.append(v)
                
                # 2. 가속도 (Acceleration) & 가가속도 (Jerk) 계산
                if last_velocity is not None:
                    a = (v - last_velocity) / dt
                    accelerations.append(a)
                    
                    if last_accel is not None:
                        j = (a - last_accel) / dt
                        jerks.append(j)
                    last_accel = a
                last_velocity = v

                # 3. 각도 변화 및 손떨림(Jitter) 계산
                angle = math.atan2(dy, dx)
                if last_angle is not None:
                    delta_angle = angle - last_angle
                    # -pi ~ pi 사이로 정규화
                    delta_angle = (delta_angle + math.pi) % (2 * math.pi) - math.pi
                    angle_changes.append(delta_angle)
                    
                    # 진행 방향이 좌/우로 바뀌었는지(부호 반전) 체크
                    if len(angle_changes) > 1:
                        if angle_changes[-1] * angle_changes[-2] < 0:
                            jitter_count += 1
							
                last_angle = angle

        # 통계값 계산 (표준편차 포함)
        def calc_std(lst, mean_val):
            if len(lst) < 2: return 0.0
            variance = sum((x - mean_val) ** 2 for x in lst) / (len(lst) - 1)
            return math.sqrt(variance)

        # 속도
        v_mean = sum(velocities) / len(velocities) if velocities else 0.0
        v_std = calc_std(velocities, v_mean)
        ac_mean = sum(angle_changes) / len(angle_changes) if angle_changes else 0.0
        ac_std = calc_std(angle_changes, ac_mean)
		
        # 클릭 지속 시간
        avg_click = sum(click_durations) / len(click_durations) if click_durations else 0.0
        std_click = calc_std(click_durations, avg_click)
        move_to_click_ratio = move_count / (down_count + 1e-5)
		
		# 가가속도									 
        j_mean = sum(jerks) / len(jerks) if jerks else 0.0
        j_std = calc_std(jerks, j_mean)
        
        # 이동 경로 효율성 계산 (총 이동 거리 / 시작점과 끝점 사이의 직선 거리)
        straight_dist = math.hypot(last_pt['x'] - first_pt['x'], last_pt['y'] - first_pt['y'])
        path_efficiency = total_distance / (straight_dist + 1e-5)
        
        # 손떨림 비율
        jitter_rate = jitter_count / (move_count + 1e-5)

        # 추출한 Feature를 모델 입력 형식에 맞게 DataFrame으로 변환
        features = {
            'velocity_mean': float(v_mean),                      # 속도 변화 평균
            'velocity_std': float(v_std),                        # 속도 변화 표준편차
            'jerk_mean': float(j_mean),                          # 가가속도 평균
            'jerk_std': float(j_std),                            # 가가속도 표준편차
            'angle_change_std': float(ac_std),                   # 각도 변화 표준편차
            'jitter_rate': float(jitter_rate),                   # 손떨림 비율
            'path_efficiency': float(path_efficiency),           # 이동 경로 효율성
            'total_distance': float(total_distance),             # 총 이동 거리
            'pause_count': int(pause_count),                     # 멈춘 횟수
            'move_to_click_ratio': float(move_to_click_ratio),   # 이동 대비 클릭 비율
            'total_clicks': int(down_count),                     # 총 클릭 수
            'avg_click_duration': float(avg_click),              # 클릭 지속 시간 평균
            'std_click_duration': float(std_click)               # 클릭 지속 시간 표준편차
        }
        
        return pd.DataFrame([features])

    def predict_macro(self, raw_data_list, threshold=MACRO_THRESHOLD):
        
        try:
            if len(raw_data_list) <= 5:
                return {
                    "success": True,
                    "is_macro": True,
                    "probability": 100.0,
                    "message": "비정상적으로 적은 마우스 이벤트"
                }
            
            features_df = self._extract_features(raw_data_list)
            
            # 모델이 로드되지 않았을 경우 방어
            if self.model is None or not hasattr(self.model, 'classes_'):
                return {"success": False, "error_message": "학습된 모델이 없습니다. Feature가 추가되었으니 재학습을 진행해 주세요."}
            
            prediction = self.model.predict(features_df)[0]
            probability = self.model.predict_proba(features_df)[0][1]
            
            # 판별 결과와 확률을 반환
            return {
                "success": True,
                "is_macro": bool(prediction >= threshold),
                "probability": round(float(probability), 4)
            }
        
        except ValueError as ve:
            return {"success": False, "error_message": str(ve)}
        except Exception as e:
            return {"success": False, "error_message": f"데이터 분석 중 오류 발생: {str(e)}"}