# ODA History Service — Test Coverage Analysis

**Generated**: 2026-07-28  
**Project**: `oda-history-service` (30 source files)  
**Existing Tests**: 5 test files, 7 test methods  
**Coverage Gap**: ~85-90% of source classes have no dedicated tests

---

## Existing Tests Summary

| Test File | What's Covered |
|---|---|
| `PrintHistoryHandlerTest.java` | CSV loading (no filters) + date/system/event filtering (2 tests) |
| `ReelResultHistoryEventHandlerTest.java` | Reel result attachment to history item (1 test) |
| `HistoryItemTest.java` | `addMedia()` on HistoryItem model (1 test) |
| `PrinterTest.java` | CSV format output from `PrintableData` (1 test) |
| `GetHistoryCommandTest.java` | Query by recipientId + system filter (2 tests) |

---

## Missing Test Cases by Component

### 1. REST Controllers (ZERO tested — 5 controllers, 0 tests)

#### AddHistoryItem (`command/AddHistoryItem.java`)
| Missing Test | Priority |
|---|---|
| Authenticated request → sends command via RabbitMQ | High |
| Unauthenticated → returns `401` | High |
| Null `paymentId` → returns `400` Bad Request | High |
| Legacy `POST /history/add` endpoint equivalence | Medium |
| RabbitMQ client failure handling | Low |

#### DeleteHistoryItem (`command/DeleteHistoryItem.java`)
| Missing Test | Priority |
|---|---|
| Authenticated + valid item → calls `markDeleted()` | High |
| Unauthenticated → returns `401` | High |
| Null `historyItemId` → returns `400` | High |
| Item not found → returns `404` | High |
| Already-deleted item (no-op?) | Medium |

#### RepeatAlert (`command/RepeatAlert.java`)
| Missing Test | Priority |
|---|---|
| Authenticated + valid item → sends `RepeatAlertCommand` via RabbitMQ | High |
| Unauthenticated → returns `401` | High |
| Null `historyItemId` → returns `400` | High |
| Item not found → returns `404` | High |
| Item with null `originId` | Medium |

#### PrintCsv (`command/PrintCsv.java`)
| Missing Test | Priority |
|---|---|
| Authenticated → returns `PrintCsvResponse` with generated printId | High |
| Unauthenticated → returns `401` | High |
| With all filters (systems, events, after, before) | Medium |
| With no filters (defaults) | Medium |

#### DownloadCsv (`command/DownloadCsv.java`)
| Missing Test | Priority |
|---|---|
| `GET /history/csv/{id}/status` → ready (`true`) | High |
| `GET /history/csv/{id}/status` → not ready (`false`) | High |
| `GET /history/csv/{id}/status` → unauthenticated (`401`) | High |
| `GET /history/csv/{id}` → ready → returns `StreamedFile` CSV | High |
| `GET /history/csv/{id}` → not ready → returns `204 No Content` | High |
| `GET /history/csv/{id}` → unauthenticated (`401`) | High |
| Cross-user access (wrong owner for printId) | Medium |

---

### 2. Event/Command Handlers (9 handlers, only 2 tested)

#### PaymentEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| Handles `PaymentEvent` → creates `HistoryItemData` (type="payment", system="ODA") | High |
| Sends `HistoryItemEvent` via `HistoryMessagingClient` after creation | High |
| Serialization error handling in `sendEvent()` | Medium |

#### AddHistoryItemHandler (ZERO tested — the most complex handler, 250 lines)
| Missing Test | Priority |
|---|---|
| **Basic**: simple add with valid `paymentId` → creates history item | High |
| **Duplicate guard**: same `paymentId` already exists → early return (no-op) | High |
| **Null paymentId**: → immediate return | High |
| **Goal tracking**: `addToGoal=true` + specific goal → calls `CountPaymentInSpecifiedGoalCommand` | High |
| **Default goal**: `addToGoal=true` + no goals → calls `CountPaymentInDefaultGoalCommand` | High |
| **Donaton trigger**: `triggerDonaton=true` → sends `ChangeDonatonCommand` | High |
| **Alert trigger**: `triggerAlert=true` → sends `CreateAlertCommand` | High |
| **Alert with media**: `alertMedia` present → passes URL to command | High |
| **Reel trigger**: `triggerReel=true` → sends `LinkReelCommand` | High |
| **Add to top**: `addToTop=true` → sends `HistoryItemEvent` | High |
| **Add to top**: `addToTop=false` → no event sent | High |
| **All triggers combined**: goals + donaton + alert + reel + top | High |
| **Null defaults**: null `authorizationTimestamp` → uses `Instant.now()` | Medium |
| **Null defaults**: null `event` → defaults to `"payment"` | Medium |
| **Null nickname** → sets `null` (not "null" string) | Medium |

#### ActionHistoryEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| Valid event → finds item by `originId`, calls `addActions()` | High |
| Null `originId` → no-op | Medium |
| `originId` not found in DB → no-op | Medium |
| Multiple actions in single event | Medium |

#### GoalHistoryEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| Valid event → finds item by `originId`, calls `addGoal()` | High |
| Null `originId` → no-op | Medium |
| `originId` not found → no-op | Medium |

#### MediaHistoryEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| Valid event → finds item by `originId`, calls `addMedia()` with Attachment | High |
| `originId` not found → no-op | Medium |

#### TwitchChannelFollowEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| New follow → creates item (type="follow", system="twitch") | High |
| Duplicate (same id + "twitch" system) → skip | High |
| No amount/message for follow events | Medium |

#### KickChannelFollowEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| New follow → creates item (type="follow", system="kick") | High |
| Duplicate → skip | High |

#### VkChannelFollowEventHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| New follow → creates item (type="follow", system="vk") | High |
| Duplicate → skip | High |

#### GetHistoryHandler (ZERO tested)
| Missing Test | Priority |
|---|---|
| Basic query by `recipientId` | High |
| Filter by `systems` list | High |
| Filter by `events` list | High |
| Filter by `after` date | High |
| Filter by `before` date | High |
| Combined all filters | High |
| Pagination with `pageable` | Medium |
| Null systems/events/after/before → no filter applied | Medium |

#### GetHistoryRequestHandler (ZERO tested — RabbitMQ RPC path)
| Missing Test | Priority |
|---|---|
| Same scenarios as `GetHistoryHandler` but via `@RabbitListener` on `history.get` queue | High |

---

### 3. GetHistory Controller (PARTIALLY tested — new GET endpoint is 0% covered)

**Tested (via `deprecatedGetHistory`):**
- Query by recipientId ✓
- Filter by single/multiple systems ✓

**Missing for the new `GET /history` endpoint:**
| Missing Test | Priority |
|---|---|
| Query by recipientId | High |
| Filter by `systems` query param | High |
| Filter by `events` query param | High |
| Filter by `after` timestamp | High |
| Filter by `before` timestamp | High |
| Combined: systems + events + after + before | High |
| Unauthenticated → returns `401` | High |
| Pagination (page, size, sort) | Medium |
| Empty systems list vs null systems | Medium |
| Empty events list vs null events | Medium |

---

### 4. HistoryItem Domain Model (80% untested)

**Tested:** `addMedia(attachment)` ✓

**Missing:**
| Missing Test | Priority |
|---|---|
| `addActions(actions)` → updates data + calls save | High |
| `addGoal(goal)` → updates data + calls save | High |
| `addReelResult(reelResult)` → updates data + calls save | High |
| `markDeleted()` → sets `deleted=true` + saves + sends `DeletedHistoryItem` via facade | High |
| `save()` when item exists (`existsById=true`) → calls `repository.update()` | High |
| `save()` when item is new (`existsById=false`) → calls `repository.save()` | High |
| Empty/null lists in add methods | Medium |

---

### 5. Printer (PARTIALLY tested)

**Tested:** `print(PrintableData)` CSV format ✓

**Missing:**
| Missing Test | Priority |
|---|---|
| `print(String printId)` → looks up map, returns `StreamedFile`, removes from map | High |
| `print(String printId)` → printId not found → throws | High |
| `loadData()` → paginated loading of all matching rows | High |
| `loadData()` → pagination with multiple pages | Medium |
| `isReady()` → printId exists and matches recipient → true | High |
| `isReady()` → printId missing → false | High |
| `isReady()` → printId exists but wrong recipient → false | High |
| CSV: null `amount` → empty amount field | High |
| CSV: null `nickname` → empty nickname | High |
| CSV: null `message` → empty message | High |
| CSV: null `levelName` → empty levelName | High |
| CSV: null `count` → empty count | Medium |
| CSV: special chars in message (semicolons, quotes, newlines) | Medium |
| CSV: multiple goals (only first displayed) | Medium |
| CSV: multiple actions comma-separated | Medium |

---

### 6. DonateStreamWebhook (ZERO tested)
| Missing Test | Priority |
|---|---|
| `type="confirm"` → returns `body.uid()` | High |
| `type="donation"` (or anything else) → creates history item | High |
| Amount parsing: `"100.50"` → Amount(100, 50, "RUB") | High |
| Amount parsing: empty/null sum → Amount(0, 0, "RUB") | High |
| Amount parsing: `"0.00"` → zero amount | Medium |
| Amount parsing: integer amounts like `"50.00"` | Medium |
| Missing nickname → null okay | Medium |
| Missing message → null okay | Medium |

---

### 7. Integration / Security / Edge Cases (CROSS-CUTTING)
| Missing Test | Priority |
|---|---|
| Cross-user isolation: user A cannot access user B's data via ANY endpoint | High |
| Unauthenticated access to all secured endpoints (401) | High |
| Empty database queries return empty pages (not errors) | Medium |
| Very large datasets / pagination boundary conditions | Low |
| RabbitMQ listener deserialization errors | Low |
| Soft-deleted items excluded from queries? (Currently no `deleted=false` filter in queries — potential bug) | ⚠️ Investigate |

---

### 8. Repository Layer (indirectly tested only)
| Missing Test | Priority |
|---|---|
| `HistoryItemRepository.create()` → saves + sends async event | High |
| `HistoryItemRepository.findByOriginId()` | Medium |
| `HistoryItemRepository.findById()` with null param | Medium |
| Soft-delete filtering in queries (deleted items should probably be excluded) | ⚠️ Investigate |

---

## Summary

| Category | Source Files | Test Files | Coverage |
|---|---|---|---|
| **REST Controllers** (AddHistoryItem, DeleteHistoryItem, RepeatAlert, PrintCsv, DownloadCsv) | 5 | 0 | **0%** |
| **Webhook** (DonateStreamWebhook) | 1 | 0 | **0%** |
| **Event Handlers** (Payment, Action, Goal, Media, Twitch, Kick, Vk, GetHistoryHandler, GetHistoryRequestHandler) | 9 | 2 | **~22%** |
| **Domain Model** (HistoryItem) | 1 | 1 | **~20%** |
| **Query/API** (GetHistory) | 2 | 1 | **~30%** |
| **CSV/Printer** | 1 | 1 | **~25%** |
| **Repository** (HistoryItemRepository) | 2 | 0 | **0% direct** |
| **Listeners** (CommandListener, EventsListener) | 2 | 0 | **0%** |

### Top Priority Missing Tests (estimated 40-50 test methods needed):

1. **AddHistoryItemHandler** — the most complex handler with 6+ branching paths (goals, donaton, alert, reel, top, duplicate guard)
2. **All REST controllers** — auth validation, error handling, and request flow
3. **DonateStreamWebhook** — external-facing endpoint with amount parsing logic
4. **HistoryItem model** — `markDeleted()`, other mutations, save logic (new vs update)
5. **GetHistory new `GET /history` endpoint** — full filter combo coverage
6. **Follow event handlers** — 3 similar handlers with dedup logic (Twitch, Kick, VK)
7. **GetHistoryHandler / GetHistoryRequestHandler** — internal RPC query paths
