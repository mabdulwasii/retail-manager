#!/bin/bash
set -e

echo "Fixing architecture violations..."

# Create necessary directories
mkdir -p src/main/java/com/princely/shopmanager/core/exception
mkdir -p src/main/java/com/princely/shopmanager/core/statemachine
mkdir -p src/main/java/com/princely/shopmanager/shared/util
mkdir -p src/main/java/com/princely/shopmanager/shared/repository/base

# Move TenantRegistrationException
if [ -f "src/main/java/com/princely/shopmanager/core/service/TenantRegistrationException.java" ]; then
  mv src/main/java/com/princely/shopmanager/core/service/TenantRegistrationException.java \
     src/main/java/com/princely/shopmanager/core/exception/TenantRegistrationException.java
  sed -i '' 's/package com.princely.shopmanager.core.service;/package com.princely.shopmanager.core.exception;/' \
     src/main/java/com/princely/shopmanager/core/exception/TenantRegistrationException.java
  find src/main/java -name "*.java" -exec sed -i '' \
    's|import com.princely.shopmanager.core.service.TenantRegistrationException|import com.princely.shopmanager.core.exception.TenantRegistrationException|g' {} \;
fi

# Move ShopStatusStateMachine
if [ -f "src/main/java/com/princely/shopmanager/core/service/ShopStatusStateMachine.java" ]; then
  mv src/main/java/com/princely/shopmanager/core/service/ShopStatusStateMachine.java \
     src/main/java/com/princely/shopmanager/core/statemachine/ShopStatusStateMachine.java
  sed -i '' 's/package com.princely.shopmanager.core.service;/package com.princely.shopmanager.core.statemachine;/' \
     src/main/java/com/princely/shopmanager/core/statemachine/ShopStatusStateMachine.java
  find src/main/java -name "*.java" -exec sed -i '' \
    's|import com.princely.shopmanager.core.service.ShopStatusStateMachine|import com.princely.shopmanager.core.statemachine.ShopStatusStateMachine|g' {} \;
fi

# Move BusinessMetrics
if [ -f "src/main/java/com/princely/shopmanager/shared/service/BusinessMetrics.java" ]; then
  mv src/main/java/com/princely/shopmanager/shared/service/BusinessMetrics.java \
     src/main/java/com/princely/shopmanager/shared/util/BusinessMetrics.java
  sed -i '' 's/package com.princely.shopmanager.shared.service;/package com.princely.shopmanager.shared.util;/' \
     src/main/java/com/princely/shopmanager/shared/util/BusinessMetrics.java
  find src/main/java -name "*.java" -exec sed -i '' \
    's|import com.princely.shopmanager.shared.service.BusinessMetrics|import com.princely.shopmanager.shared.util.BusinessMetrics|g' {} \;
fi

# Move EmailTemplate (it's a DTO, not a service)
if [ -f "src/main/java/com/princely/shopmanager/shared/service/EmailTemplate.java" ]; then
  mv src/main/java/com/princely/shopmanager/shared/service/EmailTemplate.java \
     src/main/java/com/princely/shopmanager/shared/dto/EmailTemplate.java
  sed -i '' 's/package com.princely.shopmanager.shared.service;/package com.princely.shopmanager.shared.dto;/' \
     src/main/java/com/princely/shopmanager/shared/dto/EmailTemplate.java
  find src/main/java -name "*.java" -exec sed -i '' \
    's|import com.princely.shopmanager.shared.service.EmailTemplate|import com.princely.shopmanager.shared.dto.EmailTemplate|g' {} \;
fi

# Move TenantAwareRepository to base subpackage
if [ -f "src/main/java/com/princely/shopmanager/shared/repository/TenantAwareRepository.java" ]; then
  mv src/main/java/com/princely/shopmanager/shared/repository/TenantAwareRepository.java \
     src/main/java/com/princely/shopmanager/shared/repository/base/TenantAwareRepository.java
  sed -i '' 's/package com.princely.shopmanager.shared.repository;/package com.princely.shopmanager.shared.repository.base;/' \
     src/main/java/com/princely/shopmanager/shared/repository/base/TenantAwareRepository.java
  find src/main/java -name "*.java" -exec sed -i '' \
    's|import com.princely.shopmanager.shared.repository.TenantAwareRepository|import com.princely.shopmanager.shared.repository.base.TenantAwareRepository|g' {} \;
fi

echo "Architecture fixes completed!"
echo "Now compiling to check for errors..."
./mvnw compile -B
