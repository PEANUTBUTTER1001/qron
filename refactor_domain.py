import os
import shutil

# 1. Move files
src_dir = 'core/model/src/main/java/com/peanutbutter1001/qron/core/model'
dest_dir = 'domain/src/main/java/com/peanutbutter1001/qron/domain/model'

if not os.path.exists(dest_dir):
    os.makedirs(dest_dir)

if os.path.exists(src_dir):
    for filename in os.listdir(src_dir):
        if filename.endswith('.kt'):
            src_file = os.path.join(src_dir, filename)
            dest_file = os.path.join(dest_dir, filename)
            
            with open(src_file, 'r', encoding='utf-8') as f:
                content = f.read()
                
            content = content.replace("package com.peanutbutter1001.qron.core.model", "package com.peanutbutter1001.qron.domain.model")
            
            with open(dest_file, 'w', encoding='utf-8') as f:
                f.write(content)

# 2. Update dependencies
def replace_in_file(filepath, old_str, new_str):
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    if old_str in content:
        content = content.replace(old_str, new_str)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

# Remove from domain
replace_in_file('domain/build.gradle.kts', '    implementation(project(":core:model"))\n', '')
# Add to core:database and core:vision
replace_in_file('core/database/build.gradle.kts', 'implementation(project(":core:model"))', 'implementation(project(":domain"))')
replace_in_file('core/vision/build.gradle.kts', 'implementation(project(":core:model"))', 'implementation(project(":domain"))')
# Remove from app and data
replace_in_file('app/build.gradle.kts', '    implementation(project(":core:model"))\n', '')
replace_in_file('data/build.gradle.kts', '    implementation(project(":core:model"))\n', '')

# Remove from settings.gradle.kts
replace_in_file('settings.gradle.kts', 'include(":core:model")\n', '')

# 3. Update imports across all files
for root, _, files in os.walk('.'):
    if 'build' in root or '.gradle' in root or '.git' in root or 'core\\model' in root or 'core/model' in root:
        continue
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if 'com.peanutbutter1001.qron.core.model' in content:
                content = content.replace('com.peanutbutter1001.qron.core.model', 'com.peanutbutter1001.qron.domain.model')
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)

# Clean up core:model
try:
    shutil.rmtree('core/model')
except:
    pass
