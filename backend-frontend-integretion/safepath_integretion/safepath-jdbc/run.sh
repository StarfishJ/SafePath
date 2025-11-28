#!/bin/bash

# 设置 Java 环境
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.7/libexec/openjdk.jdk/Contents/Home

# 检查 Java
if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ Java 21 未找到，尝试自动查找..."
    JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null)
    if [ -z "$JAVA_HOME" ]; then
        echo "❌ 请先安装 Java 21"
        exit 1
    fi
    echo "✅ 找到 Java: $JAVA_HOME"
fi

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未找到，请先安装 Maven"
    exit 1
fi

echo "=========================================="
echo "🚀 启动 SafePath JDBC 应用"
echo "=========================================="
echo "Java Home: $JAVA_HOME"
echo "Maven: $(mvn -version | head -1)"
echo "端口: 9090"
echo ""
echo "启动后访问:"
echo "  - 主页: http://localhost:9090"
echo "  - API:  http://localhost:9090/crime-report?action=list"
echo ""
echo "按 Ctrl+C 停止服务器"
echo "=========================================="
echo ""

# 启动 Jetty
mvn jetty:run

