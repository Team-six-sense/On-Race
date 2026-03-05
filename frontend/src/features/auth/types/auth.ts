export interface SignupResponse {
  id: number;
  email: string;
  createAt: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface AccessTokenResponse {
  accessToken: string;
  expiresIn: number;
}
