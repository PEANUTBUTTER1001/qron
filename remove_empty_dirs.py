import os

def remove_empty_dirs(path):
    deleted_any = False
    for root, dirs, files in os.walk(path, topdown=False):
        if '.git' in root or '.gradle' in root or 'build' in root:
            continue
        for name in dirs:
            dir_path = os.path.join(root, name)
            if '.git' in dir_path or '.gradle' in dir_path or 'build' in dir_path:
                continue
            if not os.listdir(dir_path):
                print(f"Removing empty directory: {dir_path}")
                os.rmdir(dir_path)
                deleted_any = True
    return deleted_any

if __name__ == "__main__":
    project_root = "."
    # Run multiple times because removing child might make parent empty
    while remove_empty_dirs(project_root):
        pass
    print("Finished removing empty directories.")
