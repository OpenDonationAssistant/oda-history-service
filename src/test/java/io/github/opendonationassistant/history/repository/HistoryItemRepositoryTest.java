package io.github.opendonationassistant.history.repository;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.model.DonatePayEuPaymentHistoryItem;
import io.github.opendonationassistant.history.model.DonatePayPaymentHistoryItem;
import io.github.opendonationassistant.history.model.DonateStreamPaymentHistoryItem;
import io.github.opendonationassistant.history.model.DonateXPaymentHistoryItem;
import io.github.opendonationassistant.history.model.DonationAlertsPaymentHistoryItem;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.model.KickFollowHistoryItem;
import io.github.opendonationassistant.history.model.KickKicksGiftedHistoryItem;
import io.github.opendonationassistant.history.model.KickSubscriptionGiftHistoryItem;
import io.github.opendonationassistant.history.model.KickSubscriptionHistoryItem;
import io.github.opendonationassistant.history.model.ODAPaymentHistoryItem;
import io.github.opendonationassistant.history.model.TributePaymentHistoryItem;
import io.github.opendonationassistant.history.model.TwitchBanHistoryItem;
import io.github.opendonationassistant.history.model.TwitchCheerHistoryItem;
import io.github.opendonationassistant.history.model.TwitchFollowHistoryItem;
import io.github.opendonationassistant.history.model.TwitchRaidHistoryItem;
import io.github.opendonationassistant.history.model.TwitchSubscriptionGiftHistoryItem;
import io.github.opendonationassistant.history.model.TwitchSubscriptionHistoryItem;
import io.github.opendonationassistant.history.model.VKLiveFollowHistoryItem;
import io.github.opendonationassistant.history.model.VKLiveSubscriptionHistoryItem;
import io.github.opendonationassistant.rabbit.RabbitClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

public class HistoryItemRepositoryTest {

  HistoryItemDataRepository dataRepository = mock(
    HistoryItemDataRepository.class
  );
  HistoryFacade facade = mock(HistoryFacade.class);
  RabbitClient commands = mock(RabbitClient.class);
  TwitchIdMappingRepository twitchIdMappingRepository = mock(
    TwitchIdMappingRepository.class
  );
  HistoryItemRepository repository = new HistoryItemRepository(
    dataRepository,
    facade,
    commands,
    twitchIdMappingRepository
  );

  @Test
  public void testConvertReturnsSubclassForEveryKnownCase() {
    var cases = List.<Object[]>of(
      new Object[] { "ODA", "payment", ODAPaymentHistoryItem.class },
      new Object[] { "Twitch", "follow", TwitchFollowHistoryItem.class },
      new Object[] { "Twitch", "raid", TwitchRaidHistoryItem.class },
      new Object[] { "Twitch", "cheer", TwitchCheerHistoryItem.class },
      new Object[] {
        "Twitch",
        "subscription-gift",
        TwitchSubscriptionGiftHistoryItem.class,
      },
      new Object[] {
        "Twitch",
        "subscription",
        TwitchSubscriptionHistoryItem.class,
      },
      new Object[] { "Twitch", "ban", TwitchBanHistoryItem.class },
      new Object[] { "kick", "follow", KickFollowHistoryItem.class },
      new Object[] {
        "Kick",
        "subscription",
        KickSubscriptionHistoryItem.class,
      },
      new Object[] {
        "Kick",
        "subscription-gift",
        KickSubscriptionGiftHistoryItem.class,
      },
      new Object[] { "Kick", "kick-gift", KickKicksGiftedHistoryItem.class },
      new Object[] { "VKLive", "follow", VKLiveFollowHistoryItem.class },
      new Object[] {
        "VKLive",
        "subscription",
        VKLiveSubscriptionHistoryItem.class,
      },
      new Object[] {
        "Donate.Stream",
        "payment",
        DonateStreamPaymentHistoryItem.class,
      },
      new Object[] {
        "DonationAlerts",
        "payment",
        DonationAlertsPaymentHistoryItem.class,
      },
      new Object[] { "DonateX", "payment", DonateXPaymentHistoryItem.class },
      new Object[] {
        "DonatePay",
        "payment",
        DonatePayPaymentHistoryItem.class,
      },
      new Object[] {
        "DonatePay.eu",
        "payment",
        DonatePayEuPaymentHistoryItem.class,
      },
      new Object[] { "Tribute", "payment", TributePaymentHistoryItem.class }
    );

    cases.forEach(entry -> {
      String system = (String) entry[0];
      String type = (String) entry[1];
      Class<? extends HistoryItem> expected = (Class<
          ? extends HistoryItem
        >) entry[2];

      var data = Instancio.of(HistoryItemData.class)
        .set(field(HistoryItemData::system), system)
        .set(field(HistoryItemData::type), type)
        .set(field(HistoryItemData::actions), List.of())
        .set(field(HistoryItemData::metadata), Map.of())
        .create();
      when(dataRepository.findById(data.id())).thenReturn(Optional.of(data));

      var item = repository.findById(data.id());

      assertTrue(item.isPresent());
      assertEquals(expected, item.get().getClass(), system + ":" + type);
    });
  }

  @Test
  public void testConvertFallsBackToBaseItemForUnknownCase() {
    var data = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::system), "Unknown")
      .set(field(HistoryItemData::type), "unknown")
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    when(dataRepository.findById(data.id())).thenReturn(Optional.of(data));

    var item = repository.findById(data.id());

    assertTrue(item.isPresent());
    assertEquals(HistoryItem.class, item.get().getClass());
  }

  @Test
  public void testConvertFallsBackToBaseItemForNullSystem() {
    var data = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::system), null)
      .set(field(HistoryItemData::type), "payment")
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    when(dataRepository.findById(data.id())).thenReturn(Optional.of(data));

    var item = repository.findById(data.id());

    assertTrue(item.isPresent());
    assertEquals(HistoryItem.class, item.get().getClass());
  }
}
