/**
 * cti-checker.js
 * AbuseIPDB API와 Redis 캐싱을 이용한 IP 평판 조회
 */
const axios = require('axios');
const redis = require('redis');
require('dotenv').config();

const redisClient = redis.createClient({
  url: `redis://${process.env.REDIS_PASSWORD ? `:${process.env.REDIS_PASSWORD}@` : ''}${process.env.REDIS_HOST}:${process.env.REDIS_PORT}`
});

redisClient.on('error', (err) => console.error('Redis Error:', err));
redisClient.connect();

const API_KEY = process.env.ABUSEIPDB_API_KEY;
const CACHE_TTL = 3600; // 1시간 캐싱

async function checkIP(ip) {
  try {
    // 1. Redis 캐시 확인
    const cachedData = await redisClient.get(`cti:${ip}`);
    if (cachedData) {
      return JSON.parse(cachedData);
    }

    // 2. AbuseIPDB API 호출
    const response = await axios.get('https://api.abuseipdb.com/api/v2/check', {
      params: { ipAddress: ip, maxAgeInDays: 90 },
      headers: { 'Key': API_KEY, 'Accept': 'application/json' }
    });

    const abuseScore = response.data.data.abuseConfidenceScore;
    const result = {
      isMalicious: abuseScore >= 80, // 80점 이상일 경우 즉시 차단 대상
      score: abuseScore
    };

    // 3. Redis 캐시 저장
    await redisClient.setEx(`cti:${ip}`, CACHE_TTL, JSON.stringify(result));
    
    return result;
  } catch (error) {
    console.error('CTI Check Error:', error.message);
    // API 장애 시 서비스 가용성을 위해 통과(false) 처리
    return { isMalicious: false, score: 0 };
  }
}

module.exports = { checkIP };