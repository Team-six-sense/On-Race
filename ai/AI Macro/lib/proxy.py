"""프록시 로드 및 Chrome 옵션 적용.

proxies.txt 파일에서 프록시 목록을 읽어 실행 시 1회 랜덤 선택하고,
undetected_chromedriver 옵션에 주입한다. 인증이 있는 프록시는 런타임에
Chrome 확장을 생성해 onAuthRequired 핸들러로 자동 응답한다.
"""
from __future__ import annotations

import json
import random
import tempfile
from pathlib import Path
from urllib.parse import urlparse


def load_proxies(path: Path) -> list[str]:
    if not path.exists():
        return []
    result = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        result.append(line)
    return result


def pick_random_proxy(proxies: list[str]) -> str | None:
    if not proxies:
        return None
    return random.choice(proxies)


def apply_proxy_to_options(options, proxy_url: str) -> Path | None:
    parsed = urlparse(proxy_url)
    scheme = (parsed.scheme or "http").lower()
    host = parsed.hostname
    port = parsed.port
    user = parsed.username
    password = parsed.password

    if not host or not port:
        raise ValueError(f"잘못된 프록시 URL: {proxy_url!r}")

    # SOCKS / 인증 없는 HTTP: 플래그만으로 충분
    if not user or not password:
        options.add_argument(f"--proxy-server={scheme}://{host}:{port}")
        return None

    # HTTP(S) + 인증: 확장을 생성해 자격 증명을 자동 응답
    # Chrome --proxy-server 는 user:pass@ 형식을 직접 받지 않는다.
    if scheme not in ("http", "https"):
        raise ValueError(
            f"인증 프록시는 http/https 스킴만 지원: {scheme!r}"
        )
    ext_dir = _build_auth_extension(host, port, user, password, scheme)
    options.add_argument(f"--proxy-server={scheme}://{host}:{port}")
    options.add_argument(f"--load-extension={ext_dir}")
    return ext_dir


def _build_auth_extension(
    host: str, port: int, user: str, password: str, scheme: str
) -> Path:
    ext_dir = Path(tempfile.mkdtemp(prefix="uc_proxy_auth_"))

    manifest = {
        "manifest_version": 3,
        "name": "Proxy Auth",
        "version": "1.0.0",
        "permissions": ["proxy", "webRequest", "webRequestAuthProvider"],
        "host_permissions": ["<all_urls>"],
        "background": {"service_worker": "background.js"},
        "minimum_chrome_version": "108",
    }

    background_js = f"""
chrome.proxy.settings.set({{
  value: {{
    mode: "fixed_servers",
    rules: {{
      singleProxy: {{
        scheme: {json.dumps(scheme)},
        host: {json.dumps(host)},
        port: {int(port)}
      }},
      bypassList: ["localhost"]
    }}
  }},
  scope: "regular"
}});

chrome.webRequest.onAuthRequired.addListener(
  (details) => ({{
    authCredentials: {{
      username: {json.dumps(user)},
      password: {json.dumps(password)}
    }}
  }}),
  {{ urls: ["<all_urls>"] }},
  ["blocking"]
);
"""

    (ext_dir / "manifest.json").write_text(
        json.dumps(manifest, indent=2), encoding="utf-8"
    )
    (ext_dir / "background.js").write_text(background_js, encoding="utf-8")
    return ext_dir
