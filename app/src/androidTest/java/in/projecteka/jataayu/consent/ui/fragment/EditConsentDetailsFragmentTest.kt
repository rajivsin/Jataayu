package `in`.projecteka.jataayu.consent.ui.fragment

import NestedScrollAction
import `in`.projecteka.jataayu.R
import `in`.projecteka.jataayu.R.id.*
import `in`.projecteka.jataayu.core.model.Consent
import `in`.projecteka.jataayu.core.model.HiType
import `in`.projecteka.jataayu.core.model.Links
import `in`.projecteka.jataayu.core.model.approveconsent.HiTypeAndLinks
import `in`.projecteka.jataayu.testUtil.AssetReaderUtil
import `in`.projecteka.jataayu.ui.activity.TestsOnlyActivity
import `in`.projecteka.jataayu.util.extension.fromJson
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.custom.recyclerView.RecyclerViewInteractions.recyclerView
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class EditConsentDetailsFragmentTest {

    @get:Rule
    var activityRule: IntentsTestRule<TestsOnlyActivity> =
        IntentsTestRule(TestsOnlyActivity::class.java, true, true)

    private lateinit var consent: Consent

    private lateinit var linkedAccounts: List<Links>

    @Before
    @Throws(Exception::class)
    fun setup() {
        consent = Gson().fromJson(
            AssetReaderUtil.asset(
                activityRule.activity.applicationContext,
                "consent_requested.json"
            )
        )

        EventBus.getDefault().postSticky(consent)

        sendLinkedAccountsAndHiTypesEvent(true)

        val editConsentDetailsFragment = EditConsentDetailsFragment()
        activityRule.activity.addFragment(editConsentDetailsFragment, R.id.fragment_container)
    }

    private fun sendLinkedAccountsAndHiTypesEvent(selectAllAccounts: Boolean) {
        val hiTypes = ArrayList<HiType>()

        for (hiType in consent.hiTypes) {
            hiTypes.add(HiType(hiType, true))
        }

        linkedAccounts = Gson().fromJson(
            AssetReaderUtil.asset(
                activityRule.activity.applicationContext,
                "links.json"
            )
        )

        linkedAccounts.forEach { link ->
            link.careContexts.forEach {
                it.contextChecked = selectAllAccounts
            }
        }
        EventBus.getDefault().postSticky(HiTypeAndLinks(hiTypes, linkedAccounts))
    }

    @Test
    fun shouldRenderConsentDetails() {
        Thread.sleep(7000)
        displayed {
            id(tv_requester_name)
            text("Dr. Lakshmi")
        }

        displayed {
            id(tv_requester_organization)
            text("AIMS")
        }

        displayed {
            id(tv_purpose_of_request)
            text("REMOTE_CONSULTING")
        }

        displayed {
            id(tv_requests_info_from)
            text("01 Jan, 2020")
        }

        displayed {
            id(tv_requests_info_to)
            text("08 Jan, 2020")
        }

        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())

        displayed {
            id(tv_expiry_date)
            text("30 Jan, 3020")
        }

        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())

        displayed {
            id(tv_expiry_time)
            text("05:25 PM")
        }

        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())

        displayed {
            id(cg_request_info_types)
            text("Condition")
            text("DiagnosticReport")
            text("Observation")
        }
        onView(withText("Condition")).check(matches(isChecked()))
        onView(withText("DiagnosticReport")).check(matches(isChecked()))
        onView(withText("Observation")).check(matches(isChecked()))

        displayed {
            id(R.id.btn_save)
            text("Save")
        }
        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())

        displayed {
            id(cb_link_all_providers)
            text("All linked providers")
        }

        onView(withId(cb_link_all_providers)).check(matches(isChecked()))

        recyclerView(R.id.rvLinkedAccounts) {
            sizeIs(3)
            atPosition(0) {
                displayed {
                    id(tv_provider_name)
                    text("Max Health Care")
                }
            }


            atPosition(1) {
                displayed {
                    allOf {
                        id(tv_reference_number)
                        text("131")
                    }

                    allOf {
                        id(tv_patient_name)
                        text("National Cancer program")
                    }

                    allOf {
                        id(cb_care_context)
                        custom(isChecked())
                    }
                }
            }

//            onView(withId(rvLinkedAccounts)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, scrollTo()))

            atPosition(2) {
                displayed {
                    allOf {
                        id(tv_reference_number)
                        text("131")
                    }

                    allOf {
                        id(tv_patient_name)
                        text("National Cancer program")
                    }

                    allOf {
                        id(cb_care_context)
                        custom(isChecked())
                    }
                }

            }
        }
    }

    @Test
    fun shouldNotEnableSaveButtonIfNoEditMade() {
        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())
        onView(withId(btn_save)).check(matches(not(isEnabled())))
    }

    @Test
    fun shouldDisableSaveButtonIfNoAccountsSelected() {
        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())
        onView(withId(cb_link_all_providers)).perform(click())
        onView(withId(btn_save)).check(matches(not(isEnabled())))
    }

    @Test
    fun shouldUncheckAllProvidersCheckboxIfAtLeastOneAccountDeselected(){
        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())
        onView(withId(rvLinkedAccounts)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        onView(withId(cb_link_all_providers)).check(matches(not(isChecked())))
    }

    @Test
    fun shouldCheckAllProvidersCheckboxIfAllAccountsSelected(){
        sendLinkedAccountsAndHiTypesEvent(false)
        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())
        onView(withId(rvLinkedAccounts)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        onView(withId(rvLinkedAccounts)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))
        onView(withId(cb_link_all_providers)).check(matches(isChecked()))
    }

    @Test
    fun shouldRenderLinkedAccounts() {
        Thread.sleep(1000)

        onView(withId(cb_link_all_providers)).check(
            matches(
                isChecked()
            )
        )

        onView(withId(rvLinkedAccounts)).perform(nestedScrollTo())

        recyclerView(rvLinkedAccounts) {
            sizeIs(3)

            atPosition(0) {
                displayed {
                    id(tv_provider_name)
                    text("Max Health Care")
                }
            }

            atPosition(1) {
                displayed {
                    allOf {
                        id(tv_reference_number)
                        text("131")
                    }
                    allOf {
                        id(tv_patient_name)
                        text("National Cancer program")
                    }
                    allOf {
                        id(cb_care_context)
                        custom(isChecked())

                    }
                }
            }
        }

    }

    private fun nestedScrollTo(): ViewAction? {
        return NestedScrollAction()
    }
}