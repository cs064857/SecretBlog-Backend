# syntax=docker/dockerfile:1
# -----------------------------------------------------------------------------
# SecretBlog
# -----------------------------------------------------------------------------

ARG SERVICE=secret-gateway
ARG SERVICE_PORT=88

# 階段1、編譯構建
FROM maven:3.8.2-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /workspace

# 複製 POM 檔案以利用 Docker Layer Cache
COPY pom.xml .
COPY secret-common/pom.xml secret-common/
COPY secret-article/pom.xml secret-article/
COPY secret-gateway/pom.xml secret-gateway/
COPY secret-user/pom.xml secret-user/
COPY secret-storage/pom.xml secret-storage/
COPY secret-search/pom.xml secret-search/

# 下載依賴
RUN mvn -B dependency:go-offline -pl ${SERVICE} -am

# 複製源代碼
COPY secret-common/src secret-common/src
COPY secret-article/src secret-article/src
COPY secret-gateway/src secret-gateway/src
COPY secret-user/src secret-user/src
COPY secret-storage/src secret-storage/src
COPY secret-search/src secret-search/src

# 執行打包(跳過測試)
RUN mvn -B package -DskipTests -pl ${SERVICE} -am \
    && JAR_PATH="$(ls -1 ${SERVICE}/target/*.jar | grep -v 'original-' | head -n 1)" \
    && cp "${JAR_PATH}" /workspace/app.jar

# 階段2、運行環境
FROM eclipse-temurin:17-jre AS runtime
ARG SERVICE_PORT
WORKDIR /app

# 設置時區
ENV TZ=Asia/Taipei
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 設置HOME環境變數
ENV HOME=/tmp

# 創建Nacos緩存目錄並賦予權限(必須在切換USER之前執行)
RUN mkdir -p /tmp/nacos/naming \
    && chmod -R 777 /tmp/nacos

# 安全性，以非root使用者執行
USER 10001

# 從構建階段複製成果物
COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE ${SERVICE_PORT}

# 啟動參數，設置 user.home 為 /tmp，讓 Nacos 客戶端可以正確創建緩存
ENTRYPOINT ["java", "-Duser.home=/tmp", "-jar", "/app/app.jar"]
