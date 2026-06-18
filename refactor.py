import os
import shutil
import re

def move_and_rename(src_dir, dest_dir, old_pkg, new_pkg):
    if not os.path.exists(src_dir):
        return
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
        
    for filename in os.listdir(src_dir):
        src_file = os.path.join(src_dir, filename)
        if os.path.isfile(src_file) and src_file.endswith('.kt'):
            dest_file = os.path.join(dest_dir, filename)
            
            with open(src_file, 'r', encoding='utf-8') as f:
                content = f.read()
                
            content = content.replace(f"package {old_pkg}", f"package {new_pkg}")
            
            with open(dest_file, 'w', encoding='utf-8') as f:
                f.write(content)
            
            os.remove(src_file)

# 1. history
move_and_rename(
    'app/src/main/java/com/peanutbutter1001/qron/ui/history',
    'feature/history/src/main/java/com/peanutbutter1001/qron/feature/history',
    'com.peanutbutter1001.qron.ui.history',
    'com.peanutbutter1001.qron.feature.history'
)

# 2. result
move_and_rename(
    'app/src/main/java/com/peanutbutter1001/qron/ui/result',
    'feature/result/src/main/java/com/peanutbutter1001/qron/feature/result',
    'com.peanutbutter1001.qron.ui.result',
    'com.peanutbutter1001.qron.feature.result'
)

# 3. scanner (ui)
move_and_rename(
    'app/src/main/java/com/peanutbutter1001/qron/ui/scanner',
    'feature/scanner/src/main/java/com/peanutbutter1001/qron/feature/scanner',
    'com.peanutbutter1001.qron.ui.scanner',
    'com.peanutbutter1001.qron.feature.scanner'
)

# 4. theme
move_and_rename(
    'app/src/main/java/com/peanutbutter1001/qron/ui/theme',
    'core/designsystem/src/main/java/com/peanutbutter1001/qron/core/designsystem/theme',
    'com.peanutbutter1001.qron.ui.theme',
    'com.peanutbutter1001.qron.core.designsystem.theme'
)

# 5. core:model
move_and_rename(
    'domain/src/main/java/com/peanutbutter1001/qron/domain/model',
    'core/model/src/main/java/com/peanutbutter1001/qron/core/model',
    'com.peanutbutter1001.qron.domain.model',
    'com.peanutbutter1001.qron.core.model'
)

# 6. core:database
move_and_rename(
    'data/src/main/java/com/peanutbutter1001/qron/data/local',
    'core/database/src/main/java/com/peanutbutter1001/qron/core/database/local',
    'com.peanutbutter1001.qron.data.local',
    'com.peanutbutter1001.qron.core.database.local'
)

# 7. core:vision
move_and_rename(
    'data/src/main/java/com/peanutbutter1001/qron/data/scanner',
    'core/vision/src/main/java/com/peanutbutter1001/qron/core/vision',
    'com.peanutbutter1001.qron.data.scanner',
    'com.peanutbutter1001.qron.core.vision'
)

# 8. feature:scan (service)
move_and_rename(
    'app/src/main/java/com/peanutbutter1001/qron/service',
    'feature/scan/src/main/java/com/peanutbutter1001/qron/feature/scan',
    'com.peanutbutter1001.qron.service',
    'com.peanutbutter1001.qron.feature.scan'
)

# Apply global imports replacement
pkg_replacements = {
    'com.peanutbutter1001.qron.ui.history': 'com.peanutbutter1001.qron.feature.history',
    'com.peanutbutter1001.qron.ui.result': 'com.peanutbutter1001.qron.feature.result',
    'com.peanutbutter1001.qron.ui.scanner': 'com.peanutbutter1001.qron.feature.scanner',
    'com.peanutbutter1001.qron.ui.theme': 'com.peanutbutter1001.qron.core.designsystem.theme',
    'com.peanutbutter1001.qron.domain.model': 'com.peanutbutter1001.qron.core.model',
    'com.peanutbutter1001.qron.data.local': 'com.peanutbutter1001.qron.core.database.local',
    'com.peanutbutter1001.qron.data.scanner': 'com.peanutbutter1001.qron.core.vision',
    'com.peanutbutter1001.qron.service': 'com.peanutbutter1001.qron.feature.scan'
}

for root, _, files in os.walk('.'):
    if 'build' in root or '.gradle' in root or '.git' in root:
        continue
    for file in files:
        if file.endswith('.kt') or file == 'AndroidManifest.xml':
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            modified = False
            for old_pkg, new_pkg in pkg_replacements.items():
                if old_pkg in content:
                    content = content.replace(old_pkg, new_pkg)
                    modified = True
                    
            if modified:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
