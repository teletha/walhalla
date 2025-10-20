-- decode_file_list.lua
-- v1.1
-- author: lzlis
-- usage: lua53.exe decode_file_list.lua input_file > output_file
-- example: lua53.exe 1fp32igvpoxnb521p9dqypak5cal0xv0 > files.txt

-- changes:
-- v1.1: Processing moved to library

local decode = require("lib/decode")

-- arguments
local fname = ...

-- body
local h = assert(io.open(fname, 'rb'))
local text = assert(h:read('*a'))
assert(h:close())

local decrypted = decode.decode_list(text)

io.write(decrypted)