-- download.lua library
-- v1.0
-- author: lzlis
local FileLists = {}
local DefaultList
local DefaultType
local AllAgesList
local file = require("lib/file")
local curl = require("lib/curl")
local decode = require("lib/decode")
_LookupCore = require("lookup/core")

--ShinyAfro CFG stuff Start
local config = require "lookup//config"
--ShinyAfro End

local function defaultlist()
  if not DefaultList then
    DefaultList = _LookupCore.filelist()
    DefaultType = _LookupCore.filelistversion()
    return DefaultList
  else
    return DefaultList
  end
end

local function getfile_raw(base, part1, part2)
  assert(#part1 == 40)
  assert(#part2 == 32)
  assert(part1:match('^%x+$'))
  assert(part2:match('^[%d%l]+$'))
  local local_dir = "Data\\Cache\\" .. base:sub(-4, -2) .. "\\" .. part1
  if not file.dir_exists(local_dir) then
    file.make_dir(local_dir)
  end
  local local_spec = local_dir .. "\\" .. part2
  if file.file_exists(local_spec) then
    return local_spec
  end
  
  local url = base .. part1 .. "/" .. part2
  curl.execute("--output", local_spec, "--compressed", url)
  return local_spec
end

local function hasfile_raw(base, part1, part2)
  assert(#part1 == 40)
  assert(#part2 == 32)
  assert(part1:match('^%x+$'))
  assert(part2:match('^[%d%l]+$'))
  local local_dir = "Data\\Cache\\" .. base:sub(-4, -2) .. "\\" .. part1
  if not file.dir_exists(local_dir) then
    return false
  end
  local local_spec = local_dir .. "\\" .. part2
  if file.file_exists(local_spec) then
    return true
  end
  
  return false
end

local lists_raw = {}
local lists = {}
local list_base = {}

local function getlist_raw(list)
  list = list or defaultlist()
  if lists_raw[list] then
    return lists_raw[list]
  end
  
  local base
  local text
  local old = list:match('(old_lists/[%w%._]+)')
  if old then
    local h = assert(io.open(old, 'rb'))
    text = assert(h:read('*a'))
    assert(h:close())
    base = "http://assets.millennium-war.com/"
  else
  
    local part1, part2--https://d3rr19rk63q7gl%.cloudfront%.net     ('(http://assets%.millennium%-war%..../)([%x]+)/([%d%l]+)')     ('(https?://[a-z0-9%.%-]/)([%x]+)/([%d%l]+)')
    base, part1, part2 = list:match('(https?://[a-z0-9%.%-]+/)([%x]+)/([%d%l]+)')
    assert(base)
    local fname = getfile_raw(base, part1, part2)
    local h = assert(io.open(fname, 'rb'))
    text = assert(h:read('*a'))
    assert(h:close())
    
    text = decode.decode_list(text)
  end
  
  lists_raw[list] = text
  list_base[list] = base
  
  return text
end

local function getlist(list)
  list = list or defaultlist()
  if lists[list] then
    return lists[list]
  end
  
  local text, base = getlist_raw(list)
  
  local entries = {}
  for part1, part2, ty, size, name in text:gmatch("(%w+),(%w+),(%w+),(%w+),([%.%w_]+)") do
    local entry = {
      part1 = part1,
      part2 = part2,
      type = ty,
      size = assert(tonumber(size)),
      name = name,
    }
    entries[name] = entry
  end
  
  lists[list] = entries
  return entries
end

local function getfile(list, fname)
  list = list or defaultlist()
  local entries = getlist(list)
  assert(entries[fname], "missing "..fname)
  local entry = entries[fname]
  local path = getfile_raw(assert(list_base[list]), entry.part1, entry.part2)
  local h = assert(io.open(path, 'rb'))
  local text = assert(h:read('*a'))
  assert(h:close())
  return text
end

local function hasfile(list, fname)
  list = list or defaultlist()
  local entries = getlist(list)
  assert(entries[fname])
  local entry = entries[fname]
  return hasfile_raw(assert(list_base[list]), entry.part1, entry.part2)
end

local function listhasfile(list, fname)
  list = list or defaultlist(list)
  local entries = getlist(list)
  return entries[fname] ~= nil
end

local function getallageslist()
  if AllAgesList then
    return AllAgesList
  else
    AllAgesList = _LookupCore.filelist("Android A")
    return AllAgesList
  end
end

local function getlisttype()
  return DefaultType
end

return {
  defaultlist = defaultlist,
  getlist = getlist,
  getlist_raw = getlist_raw,
  getfile = getfile,
  hasfile = hasfile,
  listhasfile = listhasfile,
  getlisttype = getlisttype,
  getallageslist = getallageslist,
}
