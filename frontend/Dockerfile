# 1단계: base 설정
FROM node:20-alpine AS base
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable

# 2단계: 의존성 설치
FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY package.json pnpm-lock.yaml* ./
RUN pnpm install --frozen-lockfile

# 3단계: 빌드 (환경 변수를 이 단계로 가져와야 합니다)
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# [중요] Next.js 빌드 시점에 NEXT_PUBLIC 변수가 있어야 자바스크립트에 포함됩니다.
ENV NEXT_PUBLIC_API_MODE=mock
ENV NEXT_PUBLIC_APP_URL=
ENV ACCOUNT_API_URL=
ENV MAIN_API_URL=
ENV NEXTAUTH_URL=
ENV NEXTAUTH_SECRET=
ENV GOOGLE_CLIENT_ID=
ENV GOOGLE_CLIENT_SECRET=
ENV KAKAO_CLIENT_ID=
ENV KAKAO_CLIENT_SECRET=
ENV NAVER_CLIENT_ID=
ENV NAVER_CLIENT_SECRET=

RUN pnpm run build

# 4단계: 실행
FROM base AS runner
WORKDIR /app

ENV NODE_ENV=production
# 실행 시점에도 필요한 환경 변수 설정
ENV HOSTNAME="0.0.0.0"
ENV PORT=3000

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
# standalone 결과물 복사
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000
CMD ["node", "server.js"]