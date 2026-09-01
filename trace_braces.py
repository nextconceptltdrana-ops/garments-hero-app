import sys

c = 0
for i, line in enumerate(open(sys.argv[1]).readlines(), 1):
    old_c = c
    c += line.count('{') - line.count('}')
    if old_c == 0 and c > 0:
        print(f"Top-level block opened at {i}: {line.strip()}")
    if c == 0 and old_c > 0:
        print(f"Top-level block closed at {i}: {line.strip()}")
