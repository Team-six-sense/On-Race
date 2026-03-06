export interface SignupRequest {
  email: string;
  password: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  createAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface AccessTokenRequest {
  refreshToken: string;
}

export interface AccessTokenResponse {
  accessToken: string;
  expiresIn: number;
}

export interface EmailSendCodeRequest {
  email: string;
}

export interface EmailVerifyCodeRequest {
  email: string;
}
