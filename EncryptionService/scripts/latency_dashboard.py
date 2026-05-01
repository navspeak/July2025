"""
Latency dashboard from encryption-service ROUND_TRIP logs.

Usage:
    pip install pandas seaborn matplotlib
    python scripts/latency_dashboard.py                  # reads app.log
    python scripts/latency_dashboard.py load.log         # reads load.log
    python scripts/latency_dashboard.py app.log --uri /api/v1/file/encrypt
"""

import re
import sys
import argparse
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
from pathlib import Path

LOG_PATTERN = re.compile(
    r"(?P<timestamp>\d{4}-\d{2}-\d{2}T[\d:.+-]+)\s+\S+\s+.*?ROUND_TRIP\s+"
    r"traceId=(?P<traceId>\S+)\s+"
    r"method=(?P<method>\S+)\s+"
    r"uri=(?P<uri>\S+)\s+"
    r"status=(?P<status>\d+)\s+"
    r"durationMs=(?P<durationMs>\d+)"
)


def parse_log(log_file: str) -> pd.DataFrame:
    rows = []
    with open(log_file) as f:
        for line in f:
            m = LOG_PATTERN.search(line)
            if m:
                rows.append(m.groupdict())

    if not rows:
        print(f"No ROUND_TRIP lines found in {log_file}")
        sys.exit(1)

    df = pd.DataFrame(rows)
    df["timestamp"] = pd.to_datetime(df["timestamp"], utc=True)
    df["durationMs"] = df["durationMs"].astype(int)
    df["status"] = df["status"].astype(int)
    df["success"] = df["status"] == 200
    return df


def percentile_summary(df: pd.DataFrame) -> pd.DataFrame:
    return (df[df["success"]]
            .groupby("uri")["durationMs"]
            .agg(
                count="count",
                p50=lambda x: x.quantile(0.50),
                p95=lambda x: x.quantile(0.95),
                p99=lambda x: x.quantile(0.99),
                max="max",
            )
            .reset_index()
            .round(1))


def plot_dashboard(df: pd.DataFrame, title: str):
    sns.set_theme(style="darkgrid", palette="muted")
    fig = plt.figure(figsize=(16, 12))
    fig.suptitle(title, fontsize=14, fontweight="bold")
    gs = gridspec.GridSpec(2, 3, figure=fig, hspace=0.45, wspace=0.35)

    success = df[df["success"]]
    failures = df[~df["success"]]

    # 1. Latency distribution per endpoint
    ax1 = fig.add_subplot(gs[0, :2])
    sns.kdeplot(data=success, x="durationMs", hue="uri", fill=True, alpha=0.4, ax=ax1)
    ax1.set_title("Latency distribution by endpoint")
    ax1.set_xlabel("durationMs")

    # 2. p50 / p95 / p99 bar chart per endpoint
    ax2 = fig.add_subplot(gs[0, 2])
    pct = percentile_summary(df)
    pct_melted = pct.melt(id_vars="uri", value_vars=["p50", "p95", "p99"],
                          var_name="percentile", value_name="ms")
    sns.barplot(data=pct_melted, x="percentile", y="ms", hue="uri", ax=ax2)
    ax2.set_title("Percentiles by endpoint")
    ax2.set_ylabel("ms")

    # 3. Latency over time (scatter)
    ax3 = fig.add_subplot(gs[1, :2])
    sns.scatterplot(data=success, x="timestamp", y="durationMs",
                    hue="uri", alpha=0.5, s=20, ax=ax3)
    ax3.set_title("Latency over time (200 OK only)")
    ax3.set_xlabel("")
    ax3.set_ylabel("durationMs")
    ax3.tick_params(axis="x", rotation=30)

    # 4. Status code breakdown
    ax4 = fig.add_subplot(gs[1, 2])
    status_counts = df["status"].value_counts().reset_index()
    status_counts.columns = ["status", "count"]
    status_counts["status"] = status_counts["status"].astype(str)
    sns.barplot(data=status_counts, x="status", y="count",
                hue="status", legend=False, ax=ax4)
    ax4.set_title("Response status breakdown")
    ax4.set_xlabel("HTTP status")

    return fig


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("log_file", nargs="?", default="app.log")
    parser.add_argument("--uri", help="Filter to a specific URI")
    parser.add_argument("--out", default="latency_dashboard.png")
    args = parser.parse_args()

    df = parse_log(args.log_file)
    if args.uri:
        df = df[df["uri"] == args.uri]
        if df.empty:
            print(f"No records for uri={args.uri}")
            sys.exit(1)

    # print summary table to console
    print("\n=== Percentile Summary ===")
    print(percentile_summary(df).to_string(index=False))

    if not df[~df["success"]].empty:
        print("\n=== Non-200 Responses ===")
        print(df[~df["success"]][["timestamp", "traceId", "uri", "status", "durationMs"]]
              .to_string(index=False))

    title = f"Encryption Service Latency — {Path(args.log_file).name}"
    if args.uri:
        title += f"  [{args.uri}]"

    fig = plot_dashboard(df, title)
    fig.savefig(args.out, dpi=150, bbox_inches="tight")
    print(f"\nDashboard saved to {args.out}")
    plt.show()


if __name__ == "__main__":
    main()
