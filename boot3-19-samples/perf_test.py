"""
PerfTestController Performance Test Script
===========================================
Target: boot3-19-samples /perf API
Data: 8,000,000 rows
Usage: python perf_test.py
Requirements: pip install requests
"""

import requests
import time
import json
import os
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080/perf"
REPORT_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "perf_report.md")


def call_api(method, path, params=None, timeout=300):
    """Call API and return timing + response data."""
    url = f"{BASE_URL}{path}"
    start = time.perf_counter()
    try:
        if method == "GET":
            resp = requests.get(url, params=params, timeout=timeout)
        elif method == "POST":
            resp = requests.post(url, params=params, timeout=timeout)
        elif method == "DELETE":
            resp = requests.delete(url, params=params, timeout=timeout)
        else:
            raise ValueError(f"Unsupported method: {method}")
        elapsed_ms = (time.perf_counter() - start) * 1000
        resp.encoding = "utf-8"
        try:
            data = resp.json()
        except Exception:
            data = resp.text
        return {"success": resp.status_code == 200, "status_code": resp.status_code,
                "elapsed_ms": round(elapsed_ms, 2), "data": data}
    except Exception as e:
        elapsed_ms = (time.perf_counter() - start) * 1000
        return {"success": False, "status_code": 0,
                "elapsed_ms": round(elapsed_ms, 2), "data": str(e)}


def fmt_ms(ms):
    """Format milliseconds for display."""
    return f"{ms/1000:.2f}s" if ms >= 1000 else f"{ms:.0f}ms"


class PerfTestSuite:
    """Performance test suite that runs all tests and generates report."""

    def __init__(self):
        self.results = []

    def _add(self, name, category, params_desc, result):
        entry = {
            "name": name, "category": category, "params": params_desc,
            "client_elapsed_ms": result["elapsed_ms"],
            "success": result["success"], "data": result["data"]
        }
        self.results.append(entry)
        status = "OK" if result["success"] else "FAIL"
        server_ms = ""
        if isinstance(result["data"], dict) and "totalCostMs" in result["data"]:
            server_ms = f" | server: {fmt_ms(result['data']['totalCostMs'])}"
        print(f"  [{status}] {name}: client {fmt_ms(result['elapsed_ms'])}{server_ms}")

    def test_count(self):
        """Check current data count."""
        print("\n[1/5] DATA CHECK")
        r = call_api("GET", "/count")
        if r["success"]:
            count = r["data"].get("count", 0)
            print(f"  Rows: {count:,}")
            self._add("data_count", "init", f"count={count}", r)
            return count
        print(f"  FAIL: {r['data']}")
        return 0

    def test_single_queries(self):
        """Run single-thread query tests with various parameters."""
        print("\n[2/5] SINGLE-THREAD QUERIES")
        cases = [
            ("age_20_30_L10k", "/query/single/age",
             {"minAge": 20, "maxAge": 30, "limit": 10000}),
            ("age_20_30_L50k", "/query/single/age",
             {"minAge": 20, "maxAge": 30, "limit": 50000}),
            ("age_20_30_L100k", "/query/single/age",
             {"minAge": 20, "maxAge": 30, "limit": 100000}),
            ("age_18_60_L10k", "/query/single/age",
             {"minAge": 18, "maxAge": 60, "limit": 10000}),
            ("city_beijing_L10k", "/query/single/city",
             {"city": "\u5317\u4eac", "limit": 10000}),
            ("city_beijing_L50k", "/query/single/city",
             {"city": "\u5317\u4eac", "limit": 50000}),
            ("city_beijing_L100k", "/query/single/city",
             {"city": "\u5317\u4eac", "limit": 100000}),
            ("city_shanghai_L10k", "/query/single/city",
             {"city": "\u4e0a\u6d77", "limit": 10000}),
        ]
        for name, path, params in cases:
            self._add(name, "single", str(params), call_api("GET", path, params))

    def test_parallel_queries(self):
        """Run multi-thread parallel query tests."""
        print("\n[3/5] PARALLEL QUERIES")
        cases = [
            ("cities_t5_L5k", "/query/parallel/cities",
             {"threadCount": 5, "limit": 5000}),
            ("cities_t10_L5k", "/query/parallel/cities",
             {"threadCount": 10, "limit": 5000}),
            ("cities_t10_L10k", "/query/parallel/cities",
             {"threadCount": 10, "limit": 10000}),
            ("cities_t20_L10k", "/query/parallel/cities",
             {"threadCount": 20, "limit": 10000}),
            ("age_shard_t5_L20k", "/query/parallel/age-sharding",
             {"threadCount": 5, "limit": 20000}),
            ("age_shard_t10_L20k", "/query/parallel/age-sharding",
             {"threadCount": 10, "limit": 20000}),
            ("age_shard_t10_L50k", "/query/parallel/age-sharding",
             {"threadCount": 10, "limit": 50000}),
            ("age_shard_t20_L20k", "/query/parallel/age-sharding",
             {"threadCount": 20, "limit": 20000}),
        ]
        for name, path, params in cases:
            self._add(name, "parallel", str(params), call_api("GET", path, params))

    def test_index_compare(self):
        """Run index vs no-index comparison tests (slow due to DDL)."""
        print("\n[4/5] INDEX COMPARE (slow - DDL on 8M rows)")
        cases = [
            ("idx_city_L10k", "/index/compare",
             {"column": "city", "value": "\u5317\u4eac", "limit": 10000}),
            ("idx_city_L50k", "/index/compare",
             {"column": "city", "value": "\u5317\u4eac", "limit": 50000}),
            ("idx_city_L100k", "/index/compare",
             {"column": "city", "value": "\u5317\u4eac", "limit": 100000}),
            ("idx_age_L10k", "/index/compare",
             {"column": "age", "value": "25", "limit": 10000}),
            ("idx_age_L100k", "/index/compare",
             {"column": "age", "value": "25", "limit": 100000}),
        ]
        for name, path, params in cases:
            self._add(name, "index_compare", str(params), call_api("GET", path, params))

    def test_concurrent_load(self):
        """Run client-side concurrent load tests."""
        print("\n[5/5] CONCURRENT LOAD TEST")

        def _req(_):
            return call_api("GET", "/query/single/city",
                            {"city": "\u5317\u4eac", "limit": 1000})

        for conc in [5, 10, 20, 50]:
            start = time.perf_counter()
            res_list = []
            with ThreadPoolExecutor(max_workers=conc) as exe:
                futs = [exe.submit(_req, i) for i in range(conc)]
                for f in as_completed(futs):
                    res_list.append(f.result())
            total_ms = (time.perf_counter() - start) * 1000
            ok = sum(1 for r in res_list if r["success"])
            avg_ms = sum(r["elapsed_ms"] for r in res_list) / len(res_list)
            max_ms = max(r["elapsed_ms"] for r in res_list)
            min_ms = min(r["elapsed_ms"] for r in res_list)
            name = f"concurrent_{conc}"
            print(f"  [OK] {name}: total={fmt_ms(total_ms)} "
                  f"avg={fmt_ms(avg_ms)} max={fmt_ms(max_ms)} "
                  f"min={fmt_ms(min_ms)} ok={ok}/{conc}")
            self.results.append({
                "name": name, "category": "concurrent",
                "params": f"concurrency={conc}",
                "client_elapsed_ms": round(total_ms, 2),
                "success": ok == conc,
                "data": {
                    "concurrency": conc, "success_count": ok,
                    "avg_ms": round(avg_ms, 2), "max_ms": round(max_ms, 2),
                    "min_ms": round(min_ms, 2), "total_ms": round(total_ms, 2)
                }
            })

    def generate_report(self):
        """Generate markdown performance report."""
        print("\nGENERATING REPORT...")
        L = []
        L.append("# PerfTestController \u6027\u80fd\u6d4b\u8bd5\u62a5\u544a\n")
        L.append(f"> \u751f\u6210\u65f6\u95f4\uff1a{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}  ")
        L.append("> \u6d4b\u8bd5\u5de5\u5177\uff1aPython requests + ThreadPoolExecutor  \n")

        L.append("## \u6d4b\u8bd5\u73af\u5883\n")
        L.append("| \u9879\u76ee | \u8bf4\u660e |")
        L.append("|------|------|")
        L.append("| \u6570\u636e\u5e93 | MySQL (192.168.150.151:3306/demo) |")
        L.append("| \u6570\u636e\u91cf | **8,000,000 \u6761** |")
        L.append("| \u5e94\u7528\u7aef\u53e3 | 8080 |")
        L.append("| \u6d4b\u8bd5\u65b9\u5f0f | Python HTTP \u2192 localhost |\n")

        # --- Single thread ---
        L.append("---\n## \u4e00\u3001\u5355\u7ebf\u7a0b\u67e5\u8be2\n")
        L.append("| \u6d4b\u8bd5\u573a\u666f | \u670d\u52a1\u7aef\u8017\u65f6 | \u5ba2\u6237\u7aef\u8017\u65f6 | "
                 "\u8fd4\u56de\u884c\u6570 | \u541e\u5410\u91cf(rows/s) |")
        L.append("|----------|-----------|-----------|---------|---------------|")
        for r in self.results:
            if r["category"] == "single" and r["success"]:
                d = r["data"]
                sms = d.get("totalCostMs", 0)
                rows = d.get("totalRows", 0)
                tps = int(rows / (sms / 1000)) if sms > 0 else 0
                L.append(f"| {r['name']} | {sms}ms | "
                         f"{r['client_elapsed_ms']:.0f}ms | "
                         f"{rows:,} | {tps:,} |")
        L.append("")

        # --- Parallel ---
        L.append("---\n## \u4e8c\u3001\u591a\u7ebf\u7a0b\u5e76\u884c\u67e5\u8be2\n")
        L.append("| \u6d4b\u8bd5\u573a\u666f | \u7ebf\u7a0b\u6570 | \u670d\u52a1\u7aef\u8017\u65f6 | "
                 "\u5ba2\u6237\u7aef\u8017\u65f6 | \u603b\u884c\u6570 | \u541e\u5410\u91cf(rows/s) |")
        L.append("|----------|--------|-----------|-----------|--------|---------------|")
        for r in self.results:
            if r["category"] == "parallel" and r["success"]:
                d = r["data"]
                sms = d.get("totalCostMs", 0)
                rows = d.get("totalRows", 0)
                threads = d.get("threadCount", 0)
                tps = int(rows / (sms / 1000)) if sms > 0 else 0
                L.append(f"| {r['name']} | {threads} | {sms}ms | "
                         f"{r['client_elapsed_ms']:.0f}ms | "
                         f"{rows:,} | {tps:,} |")
        L.append("")

        # --- Index compare ---
        L.append("---\n## \u4e09\u3001\u7d22\u5f15\u5bf9\u6bd4\n")
        L.append("| \u6d4b\u8bd5\u573a\u666f | \u65e0\u7d22\u5f15 | \u6709\u7d22\u5f15 | "
                 "\u63d0\u901f\u6bd4 | \u603b\u8017\u65f6(\u542bDDL) |")
        L.append("|----------|--------|--------|--------|-------------|")
        for r in self.results:
            if r["category"] == "index_compare" and r["success"]:
                d = r["data"]
                no_idx = with_idx = ratio = "-"
                if isinstance(d, dict):
                    for val in d.values():
                        if isinstance(val, dict):
                            if val.get("hasIndex") is False:
                                no_idx = f"{val.get('totalCostMs', '-')}ms"
                            elif val.get("hasIndex") is True:
                                with_idx = f"{val.get('totalCostMs', '-')}ms"
                                s = val.get("suggestion", "")
                                ratio = s.strip() if s else "-"
                L.append(f"| {r['name']} | {no_idx} | {with_idx} | "
                         f"{ratio}x | {fmt_ms(r['client_elapsed_ms'])} |")
        L.append("")

        # --- Concurrent ---
        L.append("---\n## \u56db\u3001\u5e76\u53d1\u538b\u529b\u6d4b\u8bd5\n")
        L.append("| \u5e76\u53d1\u6570 | \u603b\u8017\u65f6 | \u5e73\u5747\u54cd\u5e94 | "
                 "\u6700\u5927\u54cd\u5e94 | \u6700\u5c0f\u54cd\u5e94 | \u6210\u529f\u7387 |")
        L.append("|--------|--------|---------|---------|---------|--------|")
        for r in self.results:
            if r["category"] == "concurrent":
                d = r["data"]
                L.append(f"| {d['concurrency']} | {fmt_ms(d['total_ms'])} | "
                         f"{fmt_ms(d['avg_ms'])} | {fmt_ms(d['max_ms'])} | "
                         f"{fmt_ms(d['min_ms'])} | "
                         f"{d['success_count']}/{d['concurrency']} |")
        L.append("")

        # --- Conclusions ---
        L.append("---\n## \u4e94\u3001\u7ed3\u8bba\u4e0e\u5efa\u8bae\n")
        L.append("### \u5173\u952e\u53d1\u73b0\n")
        single_ok = [r for r in self.results
                     if r["category"] == "single" and r["success"]]
        if single_ok:
            best = min(single_ok,
                       key=lambda x: x["data"].get("totalCostMs", 99999))
            worst = max(single_ok,
                        key=lambda x: x["data"].get("totalCostMs", 0))
            L.append(f"- \u6700\u5feb\u5355\u7ebf\u7a0b\u67e5\u8be2\uff1a"
                     f"**{best['name']}** \u2192 "
                     f"{best['data']['totalCostMs']}ms")
            L.append(f"- \u6700\u6162\u5355\u7ebf\u7a0b\u67e5\u8be2\uff1a"
                     f"**{worst['name']}** \u2192 "
                     f"{worst['data']['totalCostMs']}ms")
        parallel_ok = [r for r in self.results
                       if r["category"] == "parallel" and r["success"]]
        if parallel_ok:
            bp = min(parallel_ok,
                     key=lambda x: x["data"].get("totalCostMs", 99999))
            L.append(f"- \u6700\u9ad8\u6548\u5e76\u884c\u67e5\u8be2\uff1a"
                     f"**{bp['name']}** \u2192 "
                     f"{bp['data']['totalCostMs']}ms "
                     f"({bp['data']['totalRows']:,}\u884c)")
        L.append("")
        L.append("### \u4f18\u5316\u5efa\u8bae\n")
        L.append("1. **limit\u226410000** \u65f6\u65e0\u7d22\u5f15\u5373\u53ef"
                 "\u6ee1\u8db3 <200ms \u7684\u54cd\u5e94")
        L.append("2. **\u5927\u6570\u636e\u91cf(limit\u226550000)** "
                 "\u4f18\u5148\u4f7f\u7528\u591a\u7ebf\u7a0b\u5206\u7247")
        L.append("3. **\u7d22\u5f15\u7b56\u7565**\uff1a\u7b49\u503c+\u5c0flimit"
                 "\u6709\u6548\uff1b\u8303\u56f4+\u9ad8\u547d\u4e2d\u7387\u65f6"
                 "\u7d22\u5f15\u53cd\u800c\u6709\u5bb3")
        L.append("4. **\u8986\u76d6\u7d22\u5f15**\uff1a\u907f\u514d\u56de\u8868"
                 "\uff0c\u53ef\u6839\u636e\u5b9e\u9645\u67e5\u8be2\u5b57\u6bb5"
                 "\u5efa\u8054\u5408\u7d22\u5f15")
        L.append("5. **\u5206\u9875\u4f18\u5316**\uff1a\u8d85\u5927 limit "
                 "\u5efa\u8bae\u6539\u7528 cursor-based \u5206\u9875\n")

        # --- Raw JSON ---
        L.append("---\n## \u9644\u5f55\uff1a\u539f\u59cb\u6570\u636e\n")
        L.append("<details><summary>\u70b9\u51fb\u5c55\u5f00 JSON</summary>\n")
        L.append("```json")
        export = [{
            "name": r["name"], "category": r["category"],
            "client_ms": r["client_elapsed_ms"],
            "success": r["success"], "server_data": r["data"]
        } for r in self.results]
        L.append(json.dumps(export, ensure_ascii=False, indent=2))
        L.append("```\n</details>\n")

        content = "\n".join(L)
        with open(REPORT_FILE, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  Report saved: {REPORT_FILE}")


def main():
    """Main entry point."""
    print("=" * 60)
    print("  PerfTestController Performance Test")
    print("  Data: 8M rows | Target: localhost:8080")
    print("=" * 60)

    suite = PerfTestSuite()
    count = suite.test_count()
    if count == 0:
        print("\nERROR: Cannot connect or no data! Make sure the app is running.")
        return

    suite.test_single_queries()
    suite.test_parallel_queries()
    suite.test_index_compare()
    suite.test_concurrent_load()
    suite.generate_report()

    print("\n" + "=" * 60)
    print("  ALL TESTS COMPLETED!")
    print("=" * 60)


if __name__ == "__main__":
    main()

